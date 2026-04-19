package com.ledger.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.LedgerViewModel
import com.ledger.app.ui.components.*
import com.ledger.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: LedgerViewModel,
    onPersonClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val user         by viewModel.user.collectAsState()
    val totalLent    by viewModel.totalLent.collectAsState()
    val totalBorrowed by viewModel.totalBorrowed.collectAsState()
    val netPosition  by viewModel.netPosition.collectAsState()
    val people       by viewModel.people.collectAsState()
    val groups       by viewModel.groups.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor)
            .verticalScroll(rememberScrollState()).padding(bottom = 90.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ledger", color = TextColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurpleColor, BlueColor))),
                contentAlignment = Alignment.Center
            ) {
                Text(user?.initials ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Net position hero card
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A2640), Color(0xFF162035))))
                .border(1.dp, Color(0xFF2A4060), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("NET POSITION", color = MutedColor, fontSize = 11.sp,
                    fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(4.dp))
                val netColor = if (netPosition >= 0) GreenColor else RedColor
                val netText  = if (netPosition >= 0) "+₹$netPosition" else "−₹${-netPosition}"
                Text(netText, color = netColor, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (netPosition > 0) "People owe you more than you owe 🎉"
                    else if (netPosition < 0) "You owe more than people owe you"
                    else "You're all settled up! ✨",
                    color = MutedColor, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroPill("You'll receive", "₹$totalLent", GreenColor, Modifier.weight(1f))
                    HeroPill("You'll pay", "₹$totalBorrowed", RedColor, Modifier.weight(1f))
                }
            }
        }

        // People
        if (people.isNotEmpty()) {
            SectionTitle("People")
            people.take(5).forEach { person ->
                PersonRow(person = person, onClick = onPersonClick)
            }
        }

        // Groups snapshot
        if (groups.isNotEmpty()) {
            SectionTitle("Active Groups")
            groups.take(3).forEach { group ->
                GroupRow(group = group, onClick = onGroupsClick)
            }
        }

        // Empty state
        if (people.isEmpty() && groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💸", fontSize = 48.sp)
                    Text("No transactions yet", color = MutedColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Tap + to log your first one!", color = MutedColor, fontSize = 13.sp)
                }
            }
        }
    }
}
