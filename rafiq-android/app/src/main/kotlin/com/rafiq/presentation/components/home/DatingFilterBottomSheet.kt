package com.rafiq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.Gender
import com.rafiq.domain.model.RelationshipGoal
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary

data class DatingFilterState(
    val genderPreference: String = "EVERYONE", // MALE, FEMALE, EVERYONE
    val minAge: Int = 18,
    val maxAge: Int = 50,
    val goal: RelationshipGoal? = null,
    val verifiedOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatingFilterBottomSheet(
    filterState: DatingFilterState,
    onDismiss: () -> Unit,
    onApplyFilters: (DatingFilterState) -> Unit
) {
    var selectedGender by remember { mutableStateOf(filterState.genderPreference) }
    var ageRange by remember { mutableStateOf(filterState.minAge.toFloat()..filterState.maxAge.toFloat()) }
    var selectedGoal by remember { mutableStateOf(filterState.goal) }
    var verifiedOnly by remember { mutableStateOf(filterState.verifiedOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dating Preferences", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x),
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Gender Preference
            Text("Show Me", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("MALE" to "Men", "FEMALE" to "Women", "EVERYONE" to "Everyone").forEach { (key, label) ->
                    val isSelected = selectedGender == key
                    Surface(
                        onClick = { selectedGender = key },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryAccent else Color(0xFFF1F3F4),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Age Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Age Range", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Text("${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}", fontWeight = FontWeight.Bold, color = PrimaryAccent)
            }
            RangeSlider(
                value = ageRange,
                onValueChange = { ageRange = it },
                valueRange = 18f..65f,
                colors = SliderDefaults.colors(thumbColor = PrimaryAccent, activeTrackColor = PrimaryAccent)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Verified Profiles Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_badge_check),
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verified Profiles Only", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
                Switch(
                    checked = verifiedOnly,
                    onCheckedChange = { verifiedOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryAccent)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Button
            Button(
                onClick = {
                    onApplyFilters(
                        DatingFilterState(
                            genderPreference = selectedGender,
                            minAge = ageRange.start.toInt(),
                            maxAge = ageRange.endInclusive.toInt(),
                            goal = selectedGoal,
                            verifiedOnly = verifiedOnly
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Text("Apply Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
