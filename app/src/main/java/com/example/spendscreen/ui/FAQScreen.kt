package com.example.spendscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FAQScreen(onBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔹 GENERAL
            FAQItem(
                question = "What is SpendSense?",
                answer = "SpendSense is an expense tracking app that helps you monitor spending, set budgets, and get alerts."
            )

            FAQItem(
                question = "Is my data safe?",
                answer = "Yes. Your data is stored securely on your device and linked to your account."
            )

            // 🔹 ADD TRANSACTION
            FAQItem(
                question = "How do I add a transaction?",
                answer = "Go to the Home screen and tap on Add Transaction. Enter the amount, category, payment method, and save."
            )

            FAQItem(
                question = "Can I edit or delete a transaction?",
                answer = "Yes. Open Recent Transactions, select a transaction, and choose Edit or Delete."
            )

            FAQItem(
                question = "What payment methods are supported?",
                answer = "You can select Cash, UPI, Bank Transfer, or Other while adding a transaction."
            )

            // 🔹 BUDGET
            FAQItem(
                question = "How do I set a budget?",
                answer = "Go to the Budget screen and add a monthly budget or category-wise budget."
            )

            FAQItem(
                question = "How do budget alerts work?",
                answer = "You receive alerts when you reach 90% of your budget and warnings when the budget is exceeded."
            )

            FAQItem(
                question = "What does the green, yellow, and red budget indicator mean?",
                answer = "Green means safe spending, yellow means nearing the limit, and red means the budget is exceeded."
            )

            // 🔹 ANALYTICS
            FAQItem(
                question = "What does the Analytics screen show?",
                answer = "Analytics displays charts and insights about your spending patterns and category-wise expenses."
            )

            FAQItem(
                question = "Can I view monthly spending analysis?",
                answer = "Yes. Analytics automatically groups your expenses by month and category."
            )

            // 🔹 REPORTS
            FAQItem(
                question = "What are Reports used for?",
                answer = "Reports provide a summarized view of your income and expenses over a selected period."
            )



            // 🔹 APP SETTINGS
            FAQItem(
                question = "Can I use dark mode?",
                answer = "Yes. You can enable or disable dark mode from the Settings screen."
            )

            FAQItem(
                question = "Can I delete my account?",
                answer = "Yes. Go to Settings → Delete Account. This action is permanent and cannot be undone."
            )
        }
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    Column {
        Text(
            text = question,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = answer,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
