package com.ruchitech.quicklinkcaller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.theme.*

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    trendUp: Boolean? = null,
    trendText: String? = null,
    accentColor: Color = ElectricBlue
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                if (trendUp != null) {
                    Icon(
                        imageVector = if (trendUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (trendUp) StatusWon else StatusLost,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(title, fontSize = 12.sp, color = TextSecondary)
            if (trendText != null) {
                Text(trendText, fontSize = 11.sp, color = if (trendUp == true) StatusWon else TextSecondary)
            }
        }
    }
}

@Composable
fun LeadStatusChip(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        "New" -> StatusNew to "New"
        "Contacted" -> StatusContacted to "Contacted"
        "Interested" -> StatusInterested to "Interested"
        "Negotiation" -> StatusNegotiation to "Negotiation"
        "Won" -> StatusWon to "Won"
        "Lost" -> StatusLost to "Lost"
        else -> TextDisabled to status
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = color.copy(alpha = 0.2f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun AvatarInitials(name: String, modifier: Modifier = Modifier, size: Int = 40) {
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    val bgColor = when (name.hashCode() % 6) {
        0 -> ElectricBlue
        1 -> StatusInterested
        2 -> StatusNegotiation
        3 -> StatusWon
        4 -> StatusContacted
        else -> StatusLost
    }
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifEmpty { "?" }, color = Color.White, fontSize = (size / 2.8).sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFFD700).copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(3.dp))
            Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
        }
    }
}

@Composable
fun PriorityIndicator(priority: String, modifier: Modifier = Modifier) {
    val color = when (priority) {
        "High" -> PriorityHigh
        "Medium" -> PriorityMedium
        else -> PriorityLow
    }
    Box(modifier = modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(color))
}

@Composable
fun EmptyStateView(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📋", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.2.sp
    )
}
