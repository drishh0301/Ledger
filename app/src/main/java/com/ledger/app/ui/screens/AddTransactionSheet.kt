package com.ledger.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.ledger.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (type: String, person: String, amount: Int, category: String, note: String) -> Unit
) {
    var mode     by remember { mutableStateOf("lent") }
    var person   by remember { mutableStateOf("") }
    var amount   by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("🍽 Food") }
    var note     by remember { mutableStateOf("") }
    val categories = listOf("🍽 Food", "🚗 Travel", "🏠 Rent", "🎉 Fun", "📦 Other")

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1F2E),
        dragHandle = {
            Box(modifier = Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp)).background(BorderColor))
        }) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, bottom = 34.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            Text("Log a Transaction", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Mode toggle
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D0F14)).padding(4.dp)) {
                listOf("lent" to "💸 Lent", "borrowed" to "🤲 Borrowed", "group" to "👥 Group").forEach { (key, label) ->
                    val active = mode == key
                    val tColor = when { !active -> MutedColor; key == "lent" -> GreenColor; key == "borrowed" -> RedColor; else -> BlueColor }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (active) CardColor else Color.Transparent)
                        .clickable { mode = key }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = tColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Person / Group name
            SheetField(person, { person = it }, "Person / Group", "Name or group…")

            // Amount
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("AMOUNT", color = MutedColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    placeholder = { Text("0", color = BorderColor, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                    prefix = { Text("₹", color = MutedColor, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenColor, unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color(0xFF0D0F14), unfocusedContainerColor = Color(0xFF0D0F14),
                        focusedTextColor = TextColor, unfocusedTextColor = TextColor, cursorColor = GreenColor),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }

            // Category chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("CATEGORY", color = MutedColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, modifier = Modifier.padding(bottom = 6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val active = category == cat
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (active) YellowColor.copy(0.08f) else Color.Transparent)
                            .border(1.5.dp, if (active) YellowColor else BorderColor, RoundedCornerShape(20.dp))
                            .clickable { category = cat }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(cat, color = if (active) YellowColor else MutedColor, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Note
            SheetField(note, { note = it }, "Note (optional)", "e.g. Friday dinner…")

            // Save
            Button(onClick = {
                val amt = amount.toIntOrNull() ?: 0
                if (person.isNotBlank() && amt > 0) onSave(mode, person.trim(), amt, category, note.trim())
            }, modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(GreenColor, Color(0xFF00B07A)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()) {
                Text("Save Transaction", color = BgColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = MutedColor, fontSize = 14.sp)
            }
        }
    }
}
