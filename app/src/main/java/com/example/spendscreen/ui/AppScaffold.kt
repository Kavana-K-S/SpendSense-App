package com.example.spendscreen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AddViewModel
import com.example.viewmodel.SpendViewModel
import com.example.spendscreen.utils.ImageUtils
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.spendscreen.ui.theme.SpendSenseTheme
import com.example.data.SessionManager
import androidx.compose.ui.res.painterResource
import com.example.spendscreen.R


private val BluePrimary = Color(0xFF1976D2)

@Composable
fun AppScaffold(
    addVM: AddViewModel,
    spendVM: SpendViewModel,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    var selected by remember { mutableIntStateOf(0) }
    val ctx = LocalContext.current
    val context = LocalContext.current
    val session = remember { SessionManager(context) }


    // ✅ LOAD NON-TRANSACTION DATA ONCE
    LaunchedEffect(Unit) {
        spendVM.loadProfileImage(ctx)
        spendVM.loadBudget(ctx)
        spendVM.loadCategoryBudgets(ctx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        if (spendVM.profileImage.value != null) {
                            Image(
                                bitmap = ImageUtils.imageFromBase64(
                                    spendVM.profileImage.value!!
                                ),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.wallet_logo),
                                    contentDescription = "Wallet Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }


                        Spacer(Modifier.width(10.dp))
                        Text("SpendSense")
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar(containerColor = BluePrimary) {

                NavigationBarItem(
                    selected = selected == 0,
                    onClick = { selected = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = selected == 1,
                    onClick = { selected = 1 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, null) },
                    label = { Text("Budget") }
                )

                NavigationBarItem(
                    selected = selected == 2,
                    onClick = { selected = 2 },
                    icon = { Icon(Icons.Default.Analytics, null) },
                    label = { Text("Analytics") }
                )

                NavigationBarItem(
                    selected = selected == 3,
                    onClick = { selected = 3 },
                    icon = { Icon(Icons.Default.Description, null) },
                    label = { Text("Reports") }
                )

                NavigationBarItem(
                    selected = selected == 4,
                    onClick = { selected = 4 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)

        ) {
            when (selected) {
                0 -> HomeScreen(addVM = addVM, spendVM = spendVM)
                1 -> BudgetScreen(spendVM)
                2 -> AnalyticsScreen(spendVM)
                3 -> ReportsScreen(spendVM)
                4 -> SettingsScreen(
                    vm = spendVM,
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { onThemeChanged(it) },
                    onOpenNotifications = { selected = 5 },
                    onOpenAbout = { selected = 6 },
                    onOpenFAQ = { selected = 7 }
                )
                5 -> NotificationsScreen(
                    onBack = { selected = 4 }
                )

                6 -> AboutAppScreen(
                    onBack = { selected = 4 }
                )

                7 -> FAQScreen(
                    onBack = { selected = 4 }
                )

            }
        }
    }
}
@Composable
fun SpendSenseApp(
    addVM: AddViewModel,
    spendVM: SpendViewModel
) {
    var isDarkTheme by rememberSaveable {
        mutableStateOf(false)
    }

    SpendSenseTheme(darkTheme = isDarkTheme) {
        AppScaffold(
            addVM = addVM,
            spendVM = spendVM,
            isDarkTheme = isDarkTheme,
            onThemeChanged = { isDarkTheme = it }
        )
    }
}

