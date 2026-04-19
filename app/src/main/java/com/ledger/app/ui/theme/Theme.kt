package com.ledger.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgColor      = Color(0xFF0D0F14)
val SurfaceColor = Color(0xFF161A23)
val CardColor    = Color(0xFF1E2330)
val BorderColor  = Color(0xFF2A3045)
val GreenColor   = Color(0xFF00E5A0)
val RedColor     = Color(0xFFFF4D6D)
val BlueColor    = Color(0xFF4D9FFF)
val YellowColor  = Color(0xFFFFD166)
val PurpleColor  = Color(0xFFC084FC)
val TextColor    = Color(0xFFE8ECF4)
val MutedColor   = Color(0xFF7A8399)

@Composable
fun LedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary      = GreenColor,
            secondary    = BlueColor,
            tertiary     = PurpleColor,
            background   = BgColor,
            surface      = SurfaceColor,
            onPrimary    = BgColor,
            onBackground = TextColor,
            onSurface    = TextColor,
            error        = RedColor
        ),
        content = content
    )
}
