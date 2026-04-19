package com.ledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.PersonSummary
import com.ledger.app.data.GroupEntity
import com.ledger.app.ui.theme.*

@Composable
fun Avatar(initials: String, size: Int = 42, colors: List<Color> = listOf(PurpleColor, BlueColor)) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape)
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size / 3).sp)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(), color = MutedColor, fontSize = 11.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 6.dp)
    )
}

@Composable
fun PersonRow(person: PersonSummary, onClick: () -> Unit) {
    val avatarColors = listOf(PurpleColor, BlueColor)
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(person.initials, colors = avatarColors)
        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(person.lastActivity, color = MutedColor, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        val color  = if (person.netAmount >= 0) GreenColor else RedColor
        val label  = if (person.netAmount >= 0) "+₹${person.netAmount}" else "−₹${-person.netAmount}"
        Text(label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GroupRow(group: GroupEntity, balance: Int = 0, progress: Float = 0.5f, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                    .background(BlueColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(group.emoji, fontSize = 18.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(group.members, color = MutedColor, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress    = { progress.coerceIn(0f, 1f) },
            modifier    = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
            color       = BlueColor, trackColor = BorderColor, strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun HeroPill(label: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f)).padding(12.dp)
    ) {
        Text(label, color = MutedColor, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(amount, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
