package com.ledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.ledger.app.ui.screens.*
import com.ledger.app.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: LedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LedgerApp(viewModel) }
    }
}

@Composable
fun LedgerApp(viewModel: LedgerViewModel) {
    val isLoggedIn   by viewModel.isLoggedIn.collectAsState()
    var currentTab   by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var toastMsg     by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            kotlinx.coroutines.delay(2000)
            toastMsg = null
        }
    }

    LedgerTheme {
        // Show login screen if not logged in
        if (!isLoggedIn) {
            LoginScreen(viewModel = viewModel)
            return@LedgerTheme
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .systemBarsPadding()
        ) {
            // Main screen content
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 74.dp)) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel     = viewModel,
                        onPersonClick = { currentTab = 2 },
                        onGroupsClick = { currentTab = 1 },
                        onAddClick    = { showAddSheet = true }
                    )
                    1 -> GroupsScreen(viewModel = viewModel)
                    2 -> HistoryScreen(viewModel = viewModel)
                    3 -> ProfileScreen(
                        viewModel = viewModel,
                        onLogout  = { viewModel.logout() }
                    )
                }
            }

            // Bottom navigation bar
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                NavigationBar(
                    containerColor = Color(0xFF12151E),
                    modifier       = Modifier.fillMaxWidth().height(74.dp)
                ) {
                    listOf(
                        "Home"    to "🏠",
                        "Groups"  to "👥",
                        "History" to "📋",
                        "Profile" to "👤"
                    ).forEachIndexed { index, (label, emoji) ->
                        NavigationBarItem(
                            selected = currentTab == index,
                            onClick  = { currentTab = index },
                            icon     = { Text(emoji, fontSize = 20.sp) },
                            label    = {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = GreenColor,
                                selectedTextColor   = GreenColor,
                                unselectedIconColor = MutedColor,
                                unselectedTextColor = MutedColor,
                                indicatorColor      = GreenColor.copy(alpha = 0.1f)
                            )
                        )
                    }
                }

                // FAB — floats above the nav bar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 22.dp)
                        .offset(y = (-28).dp)
                ) {
                    FloatingActionButton(
                        onClick        = { showAddSheet = true },
                        containerColor = Color.Transparent,
                        contentColor   = BgColor,
                        modifier       = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(listOf(GreenColor, Color(0xFF00B07A)))
                            ),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            modifier           = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Toast notification
            AnimatedVisibility(
                visible  = toastMsg != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit  = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenColor)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        toastMsg ?: "",
                        color      = BgColor,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Add transaction bottom sheet
        if (showAddSheet) {
            AddTransactionSheet(
                onDismiss = { showAddSheet = false },
                onSave    = { type, person, amount, category, note ->
                    viewModel.addTransaction(type, person, amount, category, note)
                    showAddSheet = false
                    toastMsg     = "✓ Transaction saved!"
                }
            )
        }
    }
}
