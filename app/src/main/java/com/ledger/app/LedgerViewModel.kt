
package com.ledger.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledger.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LedgerViewModel(app: Application) : AndroidViewModel(app) {

    private val db       = LedgerDatabase.get(app)
    private val txDao    = db.transactionDao()
    private val grpDao   = db.groupDao()
    private val expDao   = db.groupExpenseDao()
    private val userDao  = db.userDao()

    // ── Auth state ──
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _currentUser

    private val _authError = MutableStateFlow("")
    val authError: StateFlow<String> = _authError

    // ── Transactions ──
    val allTransactions: StateFlow<List<TransactionEntity>> = txDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLent: StateFlow<Int> = txDao.totalLent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBorrowed: StateFlow<Int> = txDao.totalBorrowed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val netPosition: StateFlow<Int> = combine(totalLent, totalBorrowed) { l, b -> l - b }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Filter + Search ──
    private val _filter = MutableStateFlow("All")
    val filter: StateFlow<String> = _filter

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, _filter, _search) { list, f, q ->
            list.filter { tx ->
                val matchFilter = when (f) {
                    "Lent"     -> tx.type == "lent"
                    "Borrowed" -> tx.type == "borrowed"
                    "Groups"   -> tx.type == "group"
                    "Settled"  -> tx.isSettled
                    else       -> true
                }
                val matchSearch = q.isEmpty() ||
                        tx.personName.contains(q, true) ||
                        tx.note.contains(q, true) ||
                        tx.category.contains(q, true)
                matchFilter && matchSearch
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Groups ──
    val groups: StateFlow<List<GroupEntity>> = grpDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId

    fun getExpensesForGroup(groupId: Int): Flow<List<GroupExpenseEntity>> =
        expDao.getByGroup(groupId)

    fun getTotalForGroup(groupId: Int): Flow<Int> =
        expDao.totalForGroup(groupId)

    // ── People ──
    val people: StateFlow<List<PersonSummary>> = allTransactions.map { list ->
        list.filter { !it.isSettled && it.type != "group" }
            .groupBy { it.personName }
            .map { (name, txs) ->
                val net  = txs.sumOf { if (it.type == "lent") it.amount else -it.amount }
                val last = txs.maxByOrNull { it.id }
                PersonSummary(
                    name         = name,
                    initials     = name.take(1).uppercase(),
                    lastActivity = "${last?.category ?: ""} · ${last?.date ?: ""}",
                    netAmount    = net
                )
            }
            .filter { it.netAmount != 0 }
            .sortedByDescending { kotlin.math.abs(it.netAmount) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Auth Actions ──
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            val existing = userDao.findByEmail(email)
            if (existing != null) {
                _authError.value = "Account already exists. Please log in."
                return@launch
            }
            val initials = name.split(" ").take(2).joinToString("") { it.take(1).uppercase() }
            val newUser  = UserEntity(
                name         = name,
                email        = email,
                initials     = initials,
                passwordHash = hashPassword(password)
            )
            userDao.saveUser(newUser)
            _currentUser.value = newUser
            _isLoggedIn.value  = true
            _authError.value   = ""
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val found = userDao.findByEmail(email)
            when {
                found == null -> _authError.value = "No account found with this email."
                found.passwordHash != hashPassword(password) -> _authError.value = "Incorrect password."
                else -> {
                    _currentUser.value = found
                    _isLoggedIn.value  = true
                    _authError.value   = ""
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value  = false
        _authError.value   = ""
    }

    fun clearAuthError() { _authError.value = "" }

    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Other Actions ──
    fun setFilter(f: String) { _filter.value = f }
    fun setSearch(q: String) { _search.value = q }
    fun selectGroup(id: Int?) { _selectedGroupId.value = id }

    fun addTransaction(type: String, person: String, amount: Int, category: String, note: String) {
        viewModelScope.launch {
            txDao.insert(TransactionEntity(
                type = type, personName = person, amount = amount,
                category = category, note = note, date = today()
            ))
        }
    }

    fun settleTransaction(id: Int) { viewModelScope.launch { txDao.settle(id) } }
    fun deleteTransaction(tx: TransactionEntity) { viewModelScope.launch { txDao.delete(tx) } }

    fun addGroup(name: String, emoji: String, members: String) {
        viewModelScope.launch { grpDao.insert(GroupEntity(name = name, emoji = emoji, members = members)) }
    }

    fun deleteGroup(group: GroupEntity) { viewModelScope.launch { grpDao.delete(group) } }

    fun addGroupExpense(groupId: Int, title: String, emoji: String, paidBy: String, amount: Int, splitCount: Int) {
        viewModelScope.launch {
            expDao.insert(GroupExpenseEntity(
                groupId = groupId, title = title, emoji = emoji,
                paidBy = paidBy, amount = amount, splitCount = splitCount, date = today()
            ))
        }
    }

    fun deleteGroupExpense(expense: GroupExpenseEntity) { viewModelScope.launch { expDao.delete(expense) } }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            val current  = _currentUser.value ?: return@launch
            val initials = name.split(" ").take(2).joinToString("") { it.take(1).uppercase() }
            val updated  = current.copy(name = name, email = email, initials = initials)
            userDao.saveUser(updated)
            _currentUser.value = updated
        }
    }

    private fun today() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
}

data class PersonSummary(
    val name: String,
    val initials: String,
    val lastActivity: String,
    val netAmount: Int
)