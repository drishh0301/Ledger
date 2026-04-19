package com.ledger.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.LedgerViewModel
import com.ledger.app.data.GroupEntity
import com.ledger.app.data.GroupExpenseEntity
import com.ledger.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(viewModel: LedgerViewModel) {
    val groups           by viewModel.groups.collectAsState()
    val selectedGroupId  by viewModel.selectedGroupId.collectAsState()
    var showNewGroupSheet by remember { mutableStateOf(false) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }

    if (selectedGroupId != null) {
        val group = groups.find { it.id == selectedGroupId }
        if (group != null) {
            GroupDetailScreen(
                viewModel  = viewModel,
                group      = group,
                onBack     = { viewModel.selectGroup(null) },
                onAddExpense = { showAddExpenseSheet = true }
            )
            if (showAddExpenseSheet) {
                AddExpenseSheet(
                    groupId   = group.id,
                    onDismiss = { showAddExpenseSheet = false },
                    onSave    = { title, emoji, groupId, paidBy, amount, split ->
                        viewModel.addGroupExpense(group.id, title, emoji, paidBy, amount, split)
                        showAddExpenseSheet = false
                    }
                )
            }
            return
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(bottom = 90.dp)) {
        Text("Groups", color = TextColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp))
        Text("Split expenses with anyone", color = MutedColor, fontSize = 13.sp,
            modifier = Modifier.padding(start = 22.dp, bottom = 12.dp, top = 2.dp))

        // New group button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, BlueColor, RoundedCornerShape(14.dp))
                .clickable { showNewGroupSheet = true }.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = BlueColor)
            Text("New Group", color = BlueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(8.dp))

        if (groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👥", fontSize = 40.sp)
                    Text("No groups yet", color = MutedColor, fontSize = 14.sp)
                    Text("Create one to split expenses!", color = MutedColor, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(groups) { group ->
                    GroupListRow(group = group,
                        onClick = { viewModel.selectGroup(group.id) },
                        onDelete = { viewModel.deleteGroup(group) }
                    )
                }
            }
        }
    }

    if (showNewGroupSheet) {
        NewGroupSheet(
            onDismiss = { showNewGroupSheet = false },
            onSave    = { name, emoji, members ->
                viewModel.addGroup(name, emoji, members)
                showNewGroupSheet = false
            }
        )
    }
}

@Composable
fun GroupListRow(group: GroupEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)).background(CardColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }.padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(BlueColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(group.emoji, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(group.members, color = MutedColor, fontSize = 11.sp)
            }
            IconButton(onClick = { showDelete = !showDelete }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MutedColor, modifier = Modifier.size(18.dp))
            }
        }
        if (showDelete) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedColor.copy(alpha = 0.15f)),
                contentPadding = PaddingValues()
            ) { Text("Confirm Delete", color = RedColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun GroupDetailScreen(viewModel: LedgerViewModel, group: GroupEntity, onBack: () -> Unit, onAddExpense: () -> Unit) {
    val expenses by viewModel.getExpensesForGroup(group.id).collectAsState(emptyList())
    val total    by viewModel.getTotalForGroup(group.id).collectAsState(0)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expenses", "Balances")

    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(bottom = 90.dp)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack) {
                Text("←", color = MutedColor, fontSize = 20.sp)
            }
            Text("${group.emoji} ${group.name}", color = TextColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Button(
                onClick = onAddExpense,
                colors = ButtonDefaults.buttonColors(containerColor = BlueColor.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("+ Add", color = BlueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }

        // Total card
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(14.dp)).background(CardColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total Spent", color = MutedColor, fontSize = 11.sp)
                    Text("₹$total", color = TextColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Members", color = MutedColor, fontSize = 11.sp)
                    Text(group.members.split(",").size.toString(), color = BlueColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tabs
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
            tabs.forEachIndexed { idx, label ->
                val active = selectedTab == idx
                Column(modifier = Modifier.weight(1f).clickable { selectedTab = idx }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, color = if (active) BlueColor else MutedColor, fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(if (active) BlueColor else BorderColor))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (selectedTab == 0) {
            // Expenses list
            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No expenses yet", color = MutedColor, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn {
                    items(expenses) { expense ->
                        ExpenseRow(expense = expense, onDelete = { viewModel.deleteGroupExpense(expense) })
                    }
                }
            }
        } else {
            // Balances tab - simplified per-person summary
            val memberList = group.members.split(",").map { it.trim() }
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                memberList.forEach { member ->
                    val paid = expenses.filter { it.paidBy == member }.sumOf { it.amount }
                    val share = if (expenses.isNotEmpty()) expenses.sumOf { it.amount / it.splitCount } else 0
                    val balance = paid - share
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp)).background(CardColor)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(member, color = TextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        val color = if (balance >= 0) GreenColor else RedColor
                        val label = if (balance >= 0) "gets back ₹$balance" else "owes ₹${-balance}"
                        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(expense: GroupExpenseEntity, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(12.dp)).background(CardColor)
        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
        .clickable { showDelete = !showDelete }.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(expense.emoji, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, color = TextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Paid by ${expense.paidBy} · ÷${expense.splitCount} · ${expense.date}", color = MutedColor, fontSize = 11.sp)
            }
            Text("₹${expense.amount}", color = BlueColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        if (showDelete) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedColor.copy(alpha = 0.15f)),
                contentPadding = PaddingValues()) {
                Text("🗑 Delete", color = RedColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── New Group Bottom Sheet ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupSheet(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name    by remember { mutableStateOf("") }
    var emoji   by remember { mutableStateOf("🏠") }
    var members by remember { mutableStateOf("") }
    val emojis  = listOf("🏠", "✈️", "🍕", "🎉", "💼", "🏋️", "🎓", "🚗")

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1F2E)) {
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp).padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("New Group", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Emoji picker
            Text("ICON", color = MutedColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                emojis.forEach { e ->
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (emoji == e) BlueColor.copy(0.2f) else CardColor)
                        .border(1.5.dp, if (emoji == e) BlueColor else BorderColor, RoundedCornerShape(12.dp))
                        .clickable { emoji = e }, contentAlignment = Alignment.Center) {
                        Text(e, fontSize = 20.sp)
                    }
                }
            }

            SheetField(value = name, onValueChange = { name = it }, label = "Group Name", placeholder = "e.g. Goa Trip")
            SheetField(value = members, onValueChange = { members = it }, label = "Members (comma separated)", placeholder = "Rahul, Priya, Aisha")

            Button(onClick = {
                if (name.isNotBlank() && members.isNotBlank()) onSave(name.trim(), emoji, members.trim())
            }, modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(GreenColor, Color(0xFF00B07A)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()) {
                Text("Create Group", color = BgColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MutedColor, fontSize = 14.sp)
            }
        }
    }
}

// ── Add Expense Bottom Sheet ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(groupId: Int, onDismiss: () -> Unit, onSave: (String, String, Int, String, Int, Int) -> Unit) {
    var title  by remember { mutableStateOf("") }
    var emoji  by remember { mutableStateOf("🧾") }
    var paidBy by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var split  by remember { mutableStateOf("2") }
    val emojis = listOf("🧾", "⚡", "🛒", "📶", "🍕", "🚗", "🎬", "💊")

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1F2E)) {
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp).padding(bottom = 34.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Add Expense", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                emojis.forEach { e ->
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (emoji == e) BlueColor.copy(0.2f) else CardColor)
                        .border(1.5.dp, if (emoji == e) BlueColor else BorderColor, RoundedCornerShape(12.dp))
                        .clickable { emoji = e }, contentAlignment = Alignment.Center) {
                        Text(e, fontSize = 20.sp)
                    }
                }
            }

            SheetField(title, { title = it }, "Expense Title", "e.g. Electricity Bill")
            SheetField(paidBy, { paidBy = it }, "Paid By", "Name of person who paid")
            SheetField(amount, { amount = it }, "Amount (₹)", "0", KeyboardType.Number)
            SheetField(split, { split = it }, "Split Between (count)", "2", KeyboardType.Number)

            Button(onClick = {
                val amt = amount.toIntOrNull() ?: 0
                val sp  = split.toIntOrNull() ?: 2
                if (title.isNotBlank() && paidBy.isNotBlank() && amt > 0)
                    onSave(title.trim(), emoji, groupId, paidBy.trim(), amt, sp)
            }, modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(BlueColor, PurpleColor))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()) {
                Text("Add Expense", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MutedColor, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SheetField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label.uppercase(), color = MutedColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MutedColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenColor, unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color(0xFF0D0F14), unfocusedContainerColor = Color(0xFF0D0F14),
                focusedTextColor = TextColor, unfocusedTextColor = TextColor, cursorColor = GreenColor),
            shape = RoundedCornerShape(12.dp), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
    }
}
