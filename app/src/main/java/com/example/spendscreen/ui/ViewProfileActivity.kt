package com.example.spendscreen.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import com.google.firebase.auth.FirebaseAuth
import com.example.data.ProfileRepository
import com.example.data.ProfileEntity
class ViewProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewProfileScreen(onDone = { finish() })
        }
    }
}

/* ---------------- COLORS ---------------- */

private val Purple = Color(0xFF8E5BEF)
private val LightBg = Color(0xFFF6F2FF)

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfileScreen(onDone: () -> Unit) {

    val context = LocalContext.current
    val session = SessionManager(context)

    val userId = session.getUserId()
    val repo = remember { ProfileRepository() }

    if (userId.isNullOrBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("User not logged in")
        }
        return
    }

    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
        .reference.child("profileImages/$userId.jpg")

    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    /* -------- PROFILE DATA -------- */

    var fullName by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    /* -------- LOAD DATA -------- */
    LaunchedEffect(userId) {
        try {
            // Email from FirebaseAuth
            email = FirebaseAuth.getInstance().currentUser?.email ?: ""

            // Load from ROOM
            val profile = repo.loadProfile(context, userId)

            profile?.let {
                fullName = it.name
                countryCode = it.countryCode
                phone = it.phone
                email = it.email
                occupation = it.occupation
                income = it.income
                currency = it.currency
                imageBase64 = it.imageBase64
            }

            // OPTIONAL: Load image from Firebase Storage (only if needed)
            try {
                val bytes = storage.getBytes(2_000_000).await()
                imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (_: Exception) {
                // ignore if image not found
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    /* -------- IMAGE PICKER -------- */

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val bmp = uriToBitmap(context, uri) ?: return@rememberLauncherForActivityResult

            imageBase64 = bitmapToBase64(bmp)

            scope.launch {
                try {
                    storage.putBytes(
                        Base64.decode(imageBase64, Base64.DEFAULT)
                    ).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    /* -------- DROPDOWN OPTIONS -------- */

    val countryCodes = listOf("+91 India", "+1 USA", "+44 UK")
    val occupations = listOf("Student", "Professional", "Homemaker", "Job", "Business", "Other")
    val incomes = listOf("0 - 25,000", "25,000 - 50,000", "50,000 - 75,000", "75,000 - 1,00,000", "1,00,000+")
    val currencies = listOf("INR ₹")

    var ccExpanded by remember { mutableStateOf(false) }
    var occExpanded by remember { mutableStateOf(false) }
    var incExpanded by remember { mutableStateOf(false) }
    var curExpanded by remember { mutableStateOf(false) }

    /* ---------------- UI ---------------- */

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = { Text("Your Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple),
                actions = {
                    TextButton(
                        onClick = {
                            if (!editing) {
                                editing = true
                                return@TextButton
                            }

                            scope.launch {
                                val profile = ProfileEntity(
                                    userId = userId,
                                    name = fullName.trim(),
                                    countryCode = countryCode,
                                    phone = phone.trim(),
                                    email = email,
                                    occupation = occupation,
                                    income = income,
                                    currency = currency,
                                    imageBase64 = imageBase64
                                )

                                repo.saveProfile(context, profile)

                                showSaved = true
                                editing = false
                            }

                        }
                    ) {
                        Text(if (editing) "Save" else "Edit", color = Color.White)
                    }

                }

            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* -------- AVATAR -------- */

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = editing) { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                imageBase64?.let {
                    base64ToBitmap(it)?.let { bm ->
                        Image(
                            bitmap = bm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } ?: Text("👤", fontSize = 40.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (fullName.isBlank()) "Your Name" else fullName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            /* -------- CARD -------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(20.dp)) {

                    ProfileTextField("Full Name", fullName, editing) {
                        fullName = it
                    }

                    /* PHONE */
                    Row {
                        Box {
                            OutlinedTextField(
                                value = countryCode,
                                onValueChange = {},
                                readOnly = true,
                                enabled = editing,
                                modifier = Modifier.width(100.dp),
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, null,
                                        Modifier.clickable { if (editing) ccExpanded = true })
                                }
                            )
                            DropdownMenu(ccExpanded, { ccExpanded = false }) {
                                countryCodes.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            countryCode = it.split(" ")[0]
                                            ccExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (editing) phone = it.filter(Char::isDigit) },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = editing,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    /* EMAIL (READ ONLY) */
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )



                    Spacer(Modifier.height(12.dp))

                    ProfileDropdown("Occupation", occupation, editing, occExpanded,
                        { occExpanded = true }, { occExpanded = false }, occupations) {
                        occupation = it
                    }

                    ProfileDropdown("Monthly Income", income, editing, incExpanded,
                        { incExpanded = true }, { incExpanded = false }, incomes) {
                        income = it
                    }

                    ProfileDropdown("Currency", currency, editing, curExpanded,
                        { curExpanded = true }, { curExpanded = false }, currencies) {
                        currency = it
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }

            if (showSaved) {
                Spacer(Modifier.height(8.dp))
                Text("Profile saved ✓", color = Color(0xFF2E7D32))
            }
        }
    }
}

/* ---------------- REUSABLES ---------------- */

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    enabled: Boolean,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (enabled) onChange(it) },
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
fun ProfileDropdown(
    label: String,
    value: String,
    enabled: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null,
                    Modifier.clickable { if (enabled) onExpand() })
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded, onDismiss) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        onDismiss()
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

/* ---------------- IMAGE UTILS ---------------- */

private fun uriToBitmap(ctx: android.content.Context, uri: Uri): Bitmap? =
    try { MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri) }
    catch (_: Exception) { null }

private fun bitmapToBase64(bmp: Bitmap): String {
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
}

private fun base64ToBitmap(base64: String): Bitmap? =
    try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) { null }
