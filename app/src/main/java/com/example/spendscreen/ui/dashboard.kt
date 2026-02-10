package com.example.spendscreen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AddViewModel
import com.example.viewmodel.SpendViewModel
import com.example.data.SessionManager
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        setContent {

            var isDarkTheme by remember { mutableStateOf(session.isDarkTheme()) }

            val addVM: AddViewModel = viewModel()
            val spendVM: SpendViewModel = viewModel()

            LaunchedEffect(Unit) {
                addVM.loadTransactions(this@DashboardActivity)
                spendVM.loadSummary(this@DashboardActivity)
                spendVM.loadBudget(this@DashboardActivity)
                spendVM.loadCategoryBudgets(this@DashboardActivity)
            }

            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(
                        addVM = addVM,
                        spendVM = spendVM,
                        isDarkTheme = isDarkTheme,
                        onThemeChanged = {
                            isDarkTheme = it
                            session.saveTheme(it)
                        }
                    )
                }
            }
        }

    }
}

