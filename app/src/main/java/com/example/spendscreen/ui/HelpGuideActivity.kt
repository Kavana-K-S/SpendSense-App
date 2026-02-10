package com.example.spendscreen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class HelpGuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HelpGuideScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
fun HelpGuideScreen(onBack: () -> Unit) {

    // ⭐ Firebase (optional future use — does NOT affect your current UI)
    val firestore = FirebaseFirestore.getInstance()

    var search by remember { mutableStateOf("") }

    // Your static help guide
    val sections = remember { helpGuideSections() }

    val filtered = sections.filter {
        search.isBlank() ||
                it.title.contains(search, ignoreCase = true) ||
                it.content.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Guide", fontSize = 20.sp) },
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
        ) {

            // 🔍 Search Bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search help...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📘 Scrollable help topics
            LazyColumn {
                items(filtered) { item ->
                    HelpExpandableCard(item)
                }
            }
        }
    }
}

@Composable
fun HelpExpandableCard(item: HelpItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(item.icon, contentDescription = null)

                Spacer(Modifier.width(12.dp))

                Text(
                    item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(item.content, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

// ---------------------------- //
// DATA MODEL
// ---------------------------- //

data class HelpItem(
    val title: String,
    val content: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ---------------------------- //
// STATIC HELP DATA (Safe)
// ---------------------------- //

fun helpGuideSections() = listOf(

    HelpItem(
        title = "Adding Income / Expense",
        content = """
• Go to Home tab
• Choose Income or Expense
• Enter Amount & Date
• Tap Save
""".trimIndent(),
        icon = Icons.Default.AddCircle
    ),

    HelpItem(
        title = "Setting Monthly Budget",
        content = """
• Open the Budget screen
• Add category budgets (Food, Travel, Shopping, etc.)
• Track remaining amounts
""".trimIndent(),
        icon = Icons.Default.AccountBalanceWallet
    ),

    HelpItem(
        title = "Viewing Analytics",
        content = """
• Go to Analytics
• View Income vs Expense
• View category-wise pie chart
""".trimIndent(),
        icon = Icons.Default.PieChart
    ),

    HelpItem(
        title = "Exporting Reports",
        content = """
• Open Reports
• Select date range
• Export as PDF or CSV
""".trimIndent(),
        icon = Icons.Default.Description
    ),

    HelpItem(
        title = "Editing Profile",
        content = """
• Go to Settings → View/Edit Profile
• Update your Name, Occupation, Currency
• Change or remove profile photo
""".trimIndent(),
        icon = Icons.Default.Person
    ),

    HelpItem(
        title = "Dark / Light Theme",
        content = """
• Go to Settings → App
• Toggle Dark Theme
""".trimIndent(),
        icon = Icons.Default.DarkMode
    ),

    HelpItem(
        title = "Deleting Account",
        content = """
• Go to Settings → Delete Account
• Removes all Firebase data permanently
""".trimIndent(),
        icon = Icons.Default.Delete
    )
)
