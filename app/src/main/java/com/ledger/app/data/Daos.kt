package com.ledger.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY id DESC")
    fun getByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSettled = 1 ORDER BY id DESC")
    fun getSettled(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE personName LIKE '%' || :q || '%' OR note LIKE '%' || :q || '%' OR category LIKE '%' || :q || '%' ORDER BY id DESC")
    fun search(q: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='lent' AND isSettled=0")
    fun totalLent(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='borrowed' AND isSettled=0")
    fun totalBorrowed(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)

    @Query("UPDATE transactions SET isSettled=1 WHERE id=:id")
    suspend fun settle(id: Int)

    @Delete
    suspend fun delete(tx: TransactionEntity)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY id DESC")
    fun getAll(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Query("SELECT * FROM groups WHERE id=:id")
    suspend fun getById(id: Int): GroupEntity?
}

@Dao
interface GroupExpenseDao {
    @Query("SELECT * FROM group_expenses WHERE groupId=:groupId ORDER BY id DESC")
    fun getByGroup(groupId: Int): Flow<List<GroupExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: GroupExpenseEntity)

    @Delete
    suspend fun delete(expense: GroupExpenseEntity)

    @Query("SELECT COALESCE(SUM(amount),0) FROM group_expenses WHERE groupId=:groupId")
    fun totalForGroup(groupId: Int): Flow<Int>
}


@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id=1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteUser()

    @Query("SELECT * FROM users WHERE id=1")
    suspend fun getUserOnce(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?
}
