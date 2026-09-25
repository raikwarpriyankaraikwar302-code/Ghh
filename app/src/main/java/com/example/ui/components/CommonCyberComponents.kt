package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonA
import com.example.ui.theme.ButtonB
import com.example.ui.theme.ButtonX
import com.example.ui.theme.ButtonY
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxGreen

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SurfaceBorder,
    backgroundColor: Color = SurfaceDark,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

@Composable
fun StatusBadge(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonGreen,
    inactiveColor: Color = Color(0xFF64748B)
) {
    val color = if (isActive) activeColor else inactiveColor
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GamepadKeyChip(
    buttonKey: String,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    size: Dp = 32.dp
) {
    val (bgColor, textColor, label) = when (buttonKey) {
        "BUTTON_A" -> Triple(ButtonA, Color.Black, "A")
        "BUTTON_B" -> Triple(ButtonB, Color.White, "B")
        "BUTTON_X" -> Triple(ButtonX, Color.White, "X")
        "BUTTON_Y" -> Triple(ButtonY, Color.Black, "Y")
        "LT" -> Triple(ElectricViolet, Color.White, "LT")
        "RT" -> Triple(CyberCyan, Color.Black, "RT")
        "LB" -> Triple(ElectricViolet.copy(alpha = 0.8f), Color.White, "LB")
        "RB" -> Triple(CyberCyan.copy(alpha = 0.8f), Color.Black, "RB")
        "LEFT_STICK" -> Triple(CyberCyan, Color.Black, "LS")
        "RIGHT_STICK" -> Triple(ElectricViolet, Color.White, "RS")
        "BUTTON_THUMBL" -> Triple(SurfaceDarkElevated, CyberCyan, "L3")
        "BUTTON_THUMBR" -> Triple(SurfaceDarkElevated, ElectricViolet, "R3")
        "DPAD_UP" -> Triple(SurfaceDarkElevated, TextPrimary, "▲")
        "DPAD_DOWN" -> Triple(SurfaceDarkElevated, TextPrimary, "▼")
        "DPAD_LEFT" -> Triple(SurfaceDarkElevated, TextPrimary, "◀")
        "DPAD_RIGHT" -> Triple(SurfaceDarkElevated, TextPrimary, "▶")
        else -> Triple(SurfaceDarkElevated, TextPrimary, buttonKey.take(3))
    }

    val finalBg = if (isPressed) bgColor else bgColor.copy(alpha = 0.35f)
    val finalBorder = if (isPressed) Color.White else bgColor.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(finalBg)
            .border(1.dp, finalBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) textColor else TextPrimary,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun TelemetryStat(
    label: String,
    value: String,
    unit: String,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
