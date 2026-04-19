package com.ledger.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.LedgerViewModel
import com.ledger.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: LedgerViewModel, onLogout: () -> Unit) {
    val user         by viewModel.user.collectAsState()
    val allTx        by viewModel.allTransactions.collectAsState()
    val totalLent    by viewModel.totalLent.collectAsState()
    val totalBorrowed by viewModel.totalBorrowed.collectAsState()
    val groups       by viewModel.groups.collectAsState()

    var showEditSheet  by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val activeTx  = allTx.count { !it.isSettled }
    val settledTx = allTx.count { it.isSettled }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)
        .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp).padding(bottom = 90.dp)) {

        // Avatar + name
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(PurpleColor, BlueColor))),
                contentAlignment = Alignment.Center) {
                Text(user?.initials ?: "?", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(user?.name ?: "Unknown", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "", color = MutedColor, fontSize = 13.sp)
        }

        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("$activeTx",   "Active",  GreenColor),
                Triple("${groups.size}", "Groups",  BlueColor),
                Triple("$settledTx",  "Settled", YellowColor)
            ).forEach { (value, label, color) ->
                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(CardColor)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(label, color = MutedColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Net summary
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Financial Summary", color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total lent out", color = MutedColor, fontSize = 13.sp)
                Text("₹$totalLent", color = GreenColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total borrowed", color = MutedColor, fontSize = 13.sp)
                Text("₹$totalBorrowed", color = RedColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = BorderColor)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net position", color = TextColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                val net = totalLent - totalBorrowed
                Text(if (net >= 0) "+₹$net" else "−₹${-net}", color = if (net >= 0) GreenColor else RedColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Settings items
        listOf("✏️" to "Edit Profile", "🔒" to "Privacy", "📤" to "Export Data").forEach { (emoji, label) ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                .clip(RoundedCornerShape(12.dp)).background(CardColor)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .clickable { if (label == "Edit Profile") showEditSheet = true }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(emoji, fontSize = 18.sp)
                Text(label, color = TextColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MutedColor, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Logout
        Button(onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = RedColor.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, RedColor.copy(alpha = 0.4f))) {
            Text("Log Out", color = RedColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Edit profile sheet
    if (showEditSheet) {
        EditProfileSheet(
            currentName  = user?.name ?: "",
            currentEmail = user?.email ?: "",
            onDismiss    = { showEditSheet = false },
            onSave       = { name, email -> viewModel.updateProfile(name, email); showEditSheet = false }
        )
    }

    // Logout confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = CardColor,
            title = { Text("Log Out?", color = TextColor, fontWeight = FontWeight.Bold) },
            text  = { Text("Your data will remain on this device.", color = MutedColor, fontSize = 13.sp) },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedColor)) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MutedColor)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(currentName: String, currentEmail: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name  by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1F2E)) {
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp).padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Edit Profile", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            SheetField(name,  { name = it },  "Full Name", "Your name")
            SheetField(email, { email = it }, "Email",     "your@email.com")
            Button(onClick = { if (name.isNotBlank() && email.isNotBlank()) onSave(name.trim(), email.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GreenColor, Color(0xFF00B07A)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()) {
                Text("Save Changes", color = BgColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MutedColor, fontSize = 14.sp)
            }
        }
    }
}
