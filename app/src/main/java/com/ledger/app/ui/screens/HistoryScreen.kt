package com.ledger.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.LedgerViewModel
import com.ledger.app.data.TransactionEntity
import com.ledger.app.ui.theme.*

@Composable
fun HistoryScreen(viewModel: LedgerViewModel) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery  by viewModel.search.collectAsState()
    val activeFilter by viewModel.filter.collectAsState()
    val filters = listOf("All", "Lent", "Borrowed", "Groups", "Settled")

    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(bottom = 90.dp)) {
        Text("History", color = TextColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery, onValueChange = { viewModel.setSearch(it) },
            placeholder = { Text("Search by name or category…", color = MutedColor, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenColor, unfocusedBorderColor = BorderColor,
                focusedTextColor = TextColor, unfocusedTextColor = TextColor, cursorColor = GreenColor),
            shape = RoundedCornerShape(12.dp), singleLine = true
        )

        // Filter chips
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { filter ->
                val active = activeFilter == filter
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (active) GreenColor.copy(0.08f) else Color.Transparent)
                    .border(1.5.dp, if (active) GreenColor else BorderColor, RoundedCornerShape(20.dp))
                    .clickable { viewModel.setFilter(filter) }.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(filter, color = if (active) GreenColor else MutedColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // List
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔍", fontSize = 40.sp)
                    Text("No transactions found", color = MutedColor, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn {
                items(transactions, key = { it.id }) { tx ->
                    HistoryRow(tx = tx,
                        onSettle = { viewModel.settleTransaction(tx.id) },
                        onDelete = { viewModel.deleteTransaction(tx) })
                }
            }
        }
    }
}

@Composable
fun HistoryRow(tx: TransactionEntity, onSettle: () -> Unit, onDelete: () -> Unit) {
    val dotColor    = when { tx.isSettled -> MutedColor; tx.type == "lent" -> GreenColor; tx.type == "borrowed" -> RedColor; else -> BlueColor }
    val amountColor = when { tx.isSettled -> MutedColor; tx.type == "lent" -> GreenColor; tx.type == "borrowed" -> RedColor; else -> BlueColor }
    val amountText  = when (tx.type) { "lent" -> "+₹${tx.amount}"; "borrowed" -> "−₹${tx.amount}"; else -> "₹${tx.amount}" }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CardColor.copy(alpha = if (tx.isSettled) 0.5f else 1f))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { if (!tx.isSettled) expanded = !expanded }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
            Column(modifier = Modifier.weight(1f)) {
                Text("${tx.personName} · ${tx.category}", color = if (tx.isSettled) MutedColor else TextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (tx.note.isNotEmpty()) Text(tx.note, color = MutedColor, fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                Text(tx.date, color = MutedColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amountText, color = amountColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (tx.isSettled) Text("settled ✓", color = GreenColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Expanded actions
        if (expanded && !tx.isSettled) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSettle(); expanded = false },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenColor.copy(0.15f)),
                    contentPadding = PaddingValues()) {
                    Text("✓ Settle", color = GreenColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { onDelete(); expanded = false },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedColor.copy(0.15f)),
                    contentPadding = PaddingValues()) {
                    Text("🗑 Delete", color = RedColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
