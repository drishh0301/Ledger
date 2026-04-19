package com.ledger.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.LedgerViewModel
import com.ledger.app.ui.theme.*

@Composable
fun LoginScreen(viewModel: LedgerViewModel) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(true) }
    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text("Ledger", color = TextColor, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                if (isSignUp) "Create your account" else "Welcome back",
                color = MutedColor, fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 36.dp)
            )

            // Toggle Sign Up / Log In
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardColor).padding(4.dp)
            ) {
                listOf("Sign Up" to true, "Log In" to false).forEach { (label, value) ->
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSignUp == value) SurfaceColor else Color.Transparent)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                isSignUp = value
                                viewModel.clearAuthError()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSignUp == value) TextColor else MutedColor,
                            fontSize = 14.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Name (sign up only)
            if (isSignUp) {
                AuthField(name, { name = it }, "Full Name", "Drishti Singh")
                Spacer(Modifier.height(14.dp))
            }

            // Email
            AuthField(email, { email = it }, "Email", "you@example.com", KeyboardType.Email)
            Spacer(Modifier.height(14.dp))

            // Password
            PasswordField(
                value         = password,
                onValueChange = { password = it },
                showPassword  = showPass,
                onToggleShow  = { showPass = !showPass }
            )

            // Error
            if (authError.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(authError, color = RedColor, fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    if (isSignUp) {
                        viewModel.signUp(name.trim(), email.trim(), password)
                    } else {
                        viewModel.login(email.trim(), password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GreenColor, Color(0xFF00B07A)))),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Text(
                    if (isSignUp) "Create Account" else "Log In",
                    color = BgColor, fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Your data stays on your device only",
                color = MutedColor, fontSize = 11.sp, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(), color = MutedColor, fontSize = 11.sp,
            fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MutedColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = GreenColor,
                unfocusedBorderColor    = BorderColor,
                focusedContainerColor   = CardColor,
                unfocusedContainerColor = CardColor,
                focusedTextColor        = TextColor,
                unfocusedTextColor      = TextColor,
                cursorColor             = GreenColor
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShow: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "PASSWORD", color = MutedColor, fontSize = 11.sp,
            fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text("••••••••", color = MutedColor) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    if (showPassword) "Hide" else "Show",
                    color = MutedColor, fontSize = 12.sp,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleShow
                        )
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = GreenColor,
                unfocusedBorderColor    = BorderColor,
                focusedContainerColor   = CardColor,
                unfocusedContainerColor = CardColor,
                focusedTextColor        = TextColor,
                unfocusedTextColor      = TextColor,
                cursorColor             = GreenColor
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
    }
}