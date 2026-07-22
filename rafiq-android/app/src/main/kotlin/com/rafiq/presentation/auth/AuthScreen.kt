package com.rafiq.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.autofill.ContentType
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafiq.R
import com.rafiq.presentation.components.common.RafiqTextField

@Composable
fun RafiqDropdown(
    value: String,
    label: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { expanded = true }
    ) {
        RafiqTextField(
            value = value,
            onValueChange = {},
            label = label,
            readOnly = true,
            enabled = false,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = if (expanded) com.composables.icons.lucide.R.drawable.lucide_ic_chevron_up else com.composables.icons.lucide.R.drawable.lucide_ic_chevron_down),
                    contentDescription = "Dropdown"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .heightIn(max = 250.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var autoFilledPassword by remember { mutableStateOf<String?>(null) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var country by remember { mutableStateOf("Egypt") }
    var bDay by remember { mutableStateOf("") }
    var bMonth by remember { mutableStateOf("") }
    var bYear by remember { mutableStateOf("") }

    BackHandler(enabled = !isLoginMode) {
        isLoginMode = true
        viewModel.resetState()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            if (email.isNotBlank() && password.isNotBlank() && password != autoFilledPassword) {
                // Trigger Google Password Save Bottom Sheet via CredentialManager
                com.rafiq.presentation.auth.CredentialManagerHelper.savePassword(context, email, password)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val autofillManager = context.getSystemService(android.view.autofill.AutofillManager::class.java)
                autofillManager?.commit()
            }
            onAuthSuccess()
        } else if (state is AuthState.Error) {
            val errorMsg = (state as AuthState.Error).message
            android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Reset auth state on screen entry
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.rafiq.presentation.theme.BackgroundPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Rafiq Logo",
                modifier = Modifier.size(110.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Segmented Tab Selector for Login / Sign Up (DualTone Gradient)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isLoginMode) androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent))
                                else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable {
                                if (!isLoginMode) {
                                    isLoginMode = true
                                    viewModel.resetState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isLoginMode) Color.White else com.rafiq.presentation.theme.TextPrimary.copy(alpha = 0.6f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (!isLoginMode) androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent))
                                else androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable {
                                if (isLoginMode) {
                                    isLoginMode = false
                                    viewModel.resetState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (!isLoginMode) Color.White else com.rafiq.presentation.theme.TextPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            RafiqTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, 
                    autoCorrectEnabled = false,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                autofillContentType = ContentType.EmailAddress
            )
            Spacer(modifier = Modifier.height(16.dp))

            RafiqTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, 
                    autoCorrectEnabled = false,
                    imeAction = if (isLoginMode) androidx.compose.ui.text.input.ImeAction.Done else androidx.compose.ui.text.input.ImeAction.Next
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        if (isLoginMode) {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.loginWithEmail(email, password)
                            }
                        }
                    }
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                autofillContentType = ContentType.Password,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) com.composables.icons.lucide.R.drawable.lucide_ic_eye else com.composables.icons.lucide.R.drawable.lucide_ic_eye_off),
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoginMode) {
                RafiqTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(id = if (confirmPasswordVisible) com.composables.icons.lucide.R.drawable.lucide_ic_eye else com.composables.icons.lucide.R.drawable.lucide_ic_eye_off),
                                contentDescription = "Toggle confirm password visibility"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                RafiqTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First Name",
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    autofillContentType = ContentType.PersonFirstName
                )
                Spacer(modifier = Modifier.height(16.dp))

                RafiqTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last Name",
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    autofillContentType = ContentType.PersonLastName
                )
                Spacer(modifier = Modifier.height(16.dp))

                RafiqTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            if (email.isNotBlank() && password.isNotBlank() && password == confirmPassword && firstName.isNotBlank() && lastName.isNotBlank() && username.isNotBlank() && bDay.isNotBlank() && bMonth.isNotBlank() && bYear.isNotBlank() && agreedToTerms) {
                                viewModel.signupWithEmail(email, password, firstName, lastName, username, gender, country, bDay, bMonth, bYear)
                            }
                        }
                    ),
                    autofillContentType = ContentType.Username
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                RafiqDropdown(
                    value = gender,
                    label = "Gender",
                    options = listOf("Male", "Female", "Other"),
                    onValueChange = { gender = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                RafiqDropdown(
                    value = country,
                    label = "Country",
                    options = listOf(
                        "Algeria", "Bahrain", "Comoros", "Djibouti", "Egypt", "Iraq", "Jordan", 
                        "Kuwait", "Lebanon", "Libya", "Mauritania", "Morocco", "Oman", "Palestine", 
                        "Qatar", "Saudi Arabia", "Somalia", "Sudan", "Syria", "Tunisia", 
                        "United Arab Emirates", "Yemen", "Other"
                    ),
                    onValueChange = { country = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RafiqDropdown(
                        value = bDay,
                        label = "DD",
                        options = (1..31).map { it.toString() },
                        onValueChange = { bDay = it },
                        modifier = Modifier.weight(1f)
                    )
                    RafiqDropdown(
                        value = bMonth,
                        label = "MM",
                        options = (1..12).map { it.toString() },
                        onValueChange = { bMonth = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    RafiqDropdown(
                        value = bYear,
                        label = "YYYY",
                        options = ((currentYear - 100)..(currentYear - 18)).map { it.toString() }.reversed(),
                        onValueChange = { bYear = it },
                        modifier = Modifier.weight(1.5f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("I agree to the User Agreement", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(32.dp))

                com.rafiq.presentation.components.common.DualToneButton(
                    text = "Sign Up",
                    onClick = { 
                        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || username.isBlank() || bDay.isBlank() || bMonth.isBlank() || bYear.isBlank()) {
                            android.widget.Toast.makeText(context, "Please fill in all required fields", android.widget.Toast.LENGTH_LONG).show()
                        } else if (password != confirmPassword) {
                            android.widget.Toast.makeText(context, "Passwords do not match", android.widget.Toast.LENGTH_LONG).show()
                        } else if (!agreedToTerms) {
                            android.widget.Toast.makeText(context, "Please agree to the User Agreement", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.signupWithEmail(
                                email = email,
                                password = password,
                                firstName = firstName,
                                lastName = lastName,
                                username = username,
                                genderStr = gender,
                                countryStr = country,
                                day = bDay,
                                month = bMonth,
                                year = bYear
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 60.dp
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))
                com.rafiq.presentation.components.common.DualToneButton(
                    text = "Login",
                    onClick = { viewModel.loginWithEmail(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    height = 60.dp
                )
            }

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            if (state is AuthState.NeedsExtraInfo) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Sign up successful! Please check your email and tap the link to continue.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        if (state is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }
    }
}
