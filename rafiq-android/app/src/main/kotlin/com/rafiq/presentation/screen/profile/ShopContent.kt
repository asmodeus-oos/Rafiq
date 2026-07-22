package com.rafiq.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.User

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.rafiq.util.BillingManager
import android.app.Activity

@Composable
fun ShopContent(user: User?) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val billingManager = remember {
        BillingManager(context) { productId ->
            // Success callback - should ideally call a ViewModel to update backend
            android.widget.Toast.makeText(context, "Successfully purchased $productId!", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val availableProducts by billingManager.availableProducts.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            billingManager.endConnection()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Current Balance Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_gem),
                            contentDescription = "Diamonds",
                            tint = Color(0xFF00FFFF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${user?.diamonds ?: 0}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Top Up",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Diamond Packs",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Diamond Packs
        val product100 = availableProducts.find { it.productId == "diamonds_100" }
        val product500 = availableProducts.find { it.productId == "diamonds_500" }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShopItemCard(
                modifier = Modifier.weight(1f),
                iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_gem,
                title = "100 Diamonds",
                price = product100?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99",
                onClick = { 
                    if (activity != null && product100 != null) {
                        billingManager.launchBillingFlow(activity, product100)
                    } else {
                        android.widget.Toast.makeText(context, "Product not available yet", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
            ShopItemCard(
                modifier = Modifier.weight(1f),
                iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_gem,
                title = "500 Diamonds",
                price = product500?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99",
                onClick = { 
                    if (activity != null && product500 != null) {
                        billingManager.launchBillingFlow(activity, product500)
                    } else {
                        android.widget.Toast.makeText(context, "Product not available yet", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShopItemCard(
                modifier = Modifier.weight(1f),
                iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_gem,
                title = "1000 Diamonds",
                price = "$8.99",
                isPopular = true,
                onClick = { android.widget.Toast.makeText(context, "Purchase simulation...", android.widget.Toast.LENGTH_SHORT).show() }
            )
            ShopItemCard(
                modifier = Modifier.weight(1f),
                iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_gem,
                title = "5000 Diamonds",
                price = "$39.99",
                onClick = { android.widget.Toast.makeText(context, "Purchase simulation...", android.widget.Toast.LENGTH_SHORT).show() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "VIP Tiers",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        ShopTierCard(
            title = "Gold VIP",
            price = "500 Diamonds/mo",
            features = listOf("Special Profile Badge", "Access to Voice Rooms", "Ad-Free Experience"),
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(12.dp))
        val premiumTier = availableProducts.find { it.productId == "premium_tier" }
        
        ShopTierCard(
            title = "Platinum VIP",
            price = premiumTier?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "1500 Diamonds/mo",
            features = listOf("All Gold Features", "Animated Profile Borders", "See Your Profile Visitors"),
            color = Color(0xFFE5E4E2),
            onClick = {
                if (activity != null && premiumTier != null) {
                    billingManager.launchBillingFlow(activity, premiumTier)
                } else {
                    android.widget.Toast.makeText(context, "Subscription not available yet", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ShopItemCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    price: String,
    isPopular: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Box {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF00FFFF)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (isPopular) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Text(
                        text = "POPULAR",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShopTierCard(
    title: String,
    price: String,
    features: List<String>,
    color: Color,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_crown),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = price,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_check),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
