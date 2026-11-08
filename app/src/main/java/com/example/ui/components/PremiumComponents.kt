package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.BrandRedDeep
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassFill
import com.example.ui.theme.LiveRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/** Small blinking dot used for anything that is happening right now. */
@Composable
fun PulseDot(color: Color = LiveRed, size: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .alpha(dotAlpha)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier, label: String = "LIVE") {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(LiveRed)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PulseDot(color = Color.White, size = 6.dp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
    }
}

/** Translucent pill that stays readable on top of photos and video. */
@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onClick: (() -> Unit)? = null
) {
    val base = modifier
        .clip(RoundedCornerShape(50))
        .background(GlassFill)
        .border(1.dp, GlassBorder, RoundedCornerShape(50))
    Box(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Two or three way switch that replaces the stock TabRow. */
@Composable
fun PillTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(if (selected) BrandRed else Color.Transparent, label = "tabBg")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

/** Filter chip used for sport / category rows. */
@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(if (selected) TextPrimary else DarkSurfaceCard, label = "pillBg")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (selected) TextPrimary else DarkBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF07090F) else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, count: Int? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = count.toString(), color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Primary call to action: crimson gradient with a soft border. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(BrandRed, BrandRedDeep))
    } else {
        Brush.horizontalGradient(listOf(DarkSurfaceVariant, DarkSurfaceVariant))
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = if (enabled) Color.White else TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/** Gold outlined tag, for premium and quality markers. */
@Composable
fun GoldTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, BrandGold, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/** Loading placeholder that keeps the layout stable while data arrives. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(18.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "shimmerProgress"
    )
    val brush = Brush.linearGradient(
        colors = listOf(DarkSurfaceCard, DarkSurfaceVariant, DarkSurfaceCard),
        start = Offset(progress * 1400f - 500f, 0f),
        end = Offset(progress * 1400f, 300f)
    )
    Box(modifier = modifier.clip(shape).background(brush))
}
