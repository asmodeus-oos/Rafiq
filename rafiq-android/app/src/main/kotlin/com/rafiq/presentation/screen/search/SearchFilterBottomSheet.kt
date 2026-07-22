package com.rafiq.presentation.screen.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary

data class SearchFilterState(
    val onlineOnly: Boolean = false,
    val verifiedOnly: Boolean = false,
    val selectedTier: String? = null // FREE, DIAMOND, ELITE
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBottomSheet(
    filterState: SearchFilterState,
    onDismiss: () -> Unit,
    onApplyFilters: (SearchFilterState) -> Unit
) {
    var onlineOnly by remember { mutableStateOf(filterState.onlineOnly) }
    var verifiedOnly by remember { mutableStateOf(filterState.verifiedOnly) }
    var selectedTier by remember { mutableStateOf(filterState.selectedTier) }

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
                Text("Search Filters", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Online Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Online Right Now Only", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Switch(
                    checked = onlineOnly,
                    onCheckedChange = { onlineOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryAccent)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verified Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Verified Accounts Only", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Switch(
                    checked = verifiedOnly,
                    onCheckedChange = { verifiedOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryAccent)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onApplyFilters(SearchFilterState(onlineOnly, verifiedOnly, selectedTier))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Text("Apply Search Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
