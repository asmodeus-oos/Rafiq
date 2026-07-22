package com.rafiq.presentation.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.model.User
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ModernProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val androidContext = LocalContext.current

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var ageText by remember(user) { mutableStateOf(user?.age?.toString() ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var country by remember(user) { mutableStateOf(user?.country ?: "") }
    var governorate by remember(user) { mutableStateOf(user?.governorate ?: "") }
    var showAge by remember(user) { mutableStateOf(user?.showAge ?: true) }
    var showLocation by remember(user) { mutableStateOf(user?.showLocation ?: true) }
    var hobbies by remember(user) { mutableStateOf(user?.hobbies ?: emptyList()) }
    var otherHobbyText by remember { mutableStateOf("") }
    var showHobbies by remember(user) { mutableStateOf(user?.links?.get("showHobbies")?.toBoolean() ?: true) }
    var newAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var newCoverUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            newAvatarUri = uri
        }
    }
    
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            newCoverUri = uri
        }
    }

    if (isLoading || user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x),
                        contentDescription = stringResource(com.rafiq.R.string.cancel),
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(stringResource(com.rafiq.R.string.edit_profile), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

                val coroutineScope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent)))
                        .clickable {
                            if (isSaving) return@clickable
                            isSaving = true
                            val updatedUser = user!!.copy(
                                name = name,
                                age = ageText.toIntOrNull() ?: user!!.age,
                                bio = bio,
                                phone = phone,
                                country = country,
                                governorate = governorate,
                                showAge = showAge,
                                showLocation = showLocation,
                                hobbies = hobbies,
                                links = (user?.links ?: emptyMap()) + mapOf("showHobbies" to showHobbies.toString())
                            )
                            coroutineScope.launch {
                                val skipCompression = user?.isOwner == true || user?.tier == com.rafiq.domain.model.Tier.PLATINUM || user?.tier == com.rafiq.domain.model.Tier.DIAMOND || user?.isVerified == true
                                val avatarBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    newAvatarUri?.let { uri -> 
                                        val bytes = androidContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        if (bytes != null && !skipCompression) com.rafiq.util.ImageUtils.compressImage(bytes) else bytes
                                    }
                                }
                                val coverBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    newCoverUri?.let { uri -> 
                                        val bytes = androidContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        if (bytes != null && !skipCompression) com.rafiq.util.ImageUtils.compressImage(bytes) else bytes
                                    }
                                }
                                val success = viewModel.updateProfile(updatedUser, avatarBytes, coverBytes)
                                isSaving = false
                                if (success) {
                                    onNavigateBack() 
                                } else {
                                    android.widget.Toast.makeText(androidContext, "Failed to save profile. Please check database rules.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cover Photo Edit
            EditSectionTitle("Cover Photo")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    .clickable { coverPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (newCoverUri != null || user!!.coverPhoto.isNotBlank()) {
                    AsyncImage(
                        model = newCoverUri ?: user!!.coverPhoto,
                        contentDescription = "Cover Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_image), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent)))
                        .clickable { coverPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = stringResource(com.rafiq.R.string.edit_photo), tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Edit
            EditSectionTitle("Profile Photo")
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                ) {
                    AsyncImage(
                        model = newAvatarUri ?: user!!.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent)))
                        .clickable { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = stringResource(com.rafiq.R.string.edit_photo), tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Cards
            EditSectionTitle("Basic Info")
            EditTextField(value = name, onValueChange = { name = it }, placeholder = "Full Name")
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(value = ageText, onValueChange = { ageText = it }, placeholder = "Age")
            Spacer(modifier = Modifier.height(24.dp))

            EditSectionTitle(stringResource(com.rafiq.R.string.about_me))
            EditTextField(value = bio, onValueChange = { bio = it }, placeholder = stringResource(com.rafiq.R.string.write_something_about_yourself))

            Spacer(modifier = Modifier.height(24.dp))

            EditSectionTitle("Hobbies")
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presetHobbies = listOf("Football", "Basketball", "Gym", "Running", "Gaming", "Coding", "Technology", "Reading", "Photography", "Movies", "TV Shows", "Anime", "Music", "Singing", "Dancing", "Travel", "Cooking", "Coffee", "Fashion", "Pets", "Meditation", "Volunteering", "Entrepreneurship", "Concerts", "Fitness")
                presetHobbies.forEach { hobby ->
                    val selected = hobbies.contains(hobby)
                    val bgBrush = if (selected) {
                        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent))
                    } else {
                        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                    }
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgBrush)
                            .clickable { hobbies = if (selected) hobbies - hobby else hobbies + hobby },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(hobby, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
                
                hobbies.filter { it !in presetHobbies }.forEach { hobby ->
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(com.rafiq.presentation.theme.PrimaryAccent, com.rafiq.presentation.theme.TertiaryAccent)))
                            .clickable { hobbies = hobbies - hobby },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$hobby (x)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                EditTextField(
                    value = otherHobbyText, 
                    onValueChange = { otherHobbyText = it }, 
                    placeholder = "Other Hobby", 
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                com.rafiq.presentation.components.common.DualToneButton(
                    text = "Add",
                    onClick = { 
                        if (otherHobbyText.isNotBlank() && !hobbies.contains(otherHobbyText)) {
                            hobbies = hobbies + otherHobbyText
                            otherHobbyText = ""
                        }
                    },
                    height = 48.dp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Show Hobbies on Profile", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                Switch(checked = showHobbies, onCheckedChange = { showHobbies = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            EditSectionTitle(stringResource(com.rafiq.R.string.location))
            com.rafiq.presentation.auth.RafiqDropdown(
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
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(value = governorate, onValueChange = { governorate = it }, placeholder = stringResource(com.rafiq.R.string.city_state))

            Spacer(modifier = Modifier.height(24.dp))

            EditSectionTitle(stringResource(com.rafiq.R.string.contact))
            EditTextField(value = phone, onValueChange = { phone = it }, placeholder = stringResource(com.rafiq.R.string.phone_number))

            Spacer(modifier = Modifier.height(32.dp))

            EditSectionTitle(stringResource(com.rafiq.R.string.control_your_profile))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    EditToggleRow(stringResource(com.rafiq.R.string.show_my_age), showAge) { showAge = it }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                    EditToggleRow(stringResource(com.rafiq.R.string.show_my_location), showLocation) { showLocation = it }
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun EditSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun EditToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}


