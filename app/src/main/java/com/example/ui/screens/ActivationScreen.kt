package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MantisViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ActivationScreen(viewModel: MantisViewModel) {
    val context = LocalContext.current
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val hasOverlayPerm = remember(isOverlayActive) { viewModel.hasOverlayPermission(context) }

    var pairingPort by remember { mutableStateOf("41285") }
    var pairingCode by remember { mutableStateOf("849201") }
    var isPairingSimulated by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Status Banner
        item {
            val isFullyActive = isAccessibilityActive || isPairingSimulated

            CyberCard(
                borderColor = if (isFullyActive) NeonGreen else CyberCyan,
                backgroundColor = SurfaceDarkElevated
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isFullyActive) NeonGreen.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFullyActive) Icons.Default.CheckCircle else Icons.Default.Speed,
                                    contentDescription = "Mantis Status",
                                    tint = if (isFullyActive) NeonGreen else CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isFullyActive) "Mantis Engine: ACTIVATED" else "Activation Recommended",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (isFullyActive) "Ultra-Low Latency direct touch injection ready" else "Choose an activation method below",
                                    color = if (isFullyActive) NeonGreen else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        StatusBadge(
                            text = if (isFullyActive) "ACTIVE" else "STANDBY",
                            isActive = isFullyActive
                        )
                    }
                }
            }
        }

        // Method 1: Accessibility Service (Zero Root / No PC Needed)
        item {
            CyberCard(
                borderColor = if (isAccessibilityActive) NeonGreen else SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "1. Accessibility Touch Service",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Direct on-device touch injection without PC",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        StatusBadge(
                            text = if (isAccessibilityActive) "CONNECTED" else "NOT ENABLED",
                            isActive = isAccessibilityActive
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Allows Mantis to dispatch controller events as real screen taps directly into any game. Enable 'Mantis Low-Latency Touch Injector' in Android Settings.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.openAccessibilitySettings(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityActive) SurfaceDarkElevated else CyberCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enable_accessibility_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = if (isAccessibilityActive) CyberCyan else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAccessibilityActive) "Accessibility Active (Tap to Review)" else "Open Accessibility Settings",
                            color = if (isAccessibilityActive) TextPrimary else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Method 2: Floating HUD Overlay Permission
        item {
            CyberCard(
                borderColor = if (hasOverlayPerm) NeonGreen else SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = ElectricViolet,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "2. Floating Overlay Widget",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Display in-game controller HUD & quick remapper",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        StatusBadge(
                            text = if (hasOverlayPerm) "GRANTED" else "REQUIRED",
                            isActive = hasOverlayPerm
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Allows Mantis to float over gaming sessions (like COD Mobile or Genshin Impact) so you can adjust deadzones and remap keys on the fly.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.toggleOverlay(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverlayActive) NeonGreen else ElectricViolet
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_overlay_button")
                    ) {
                        Text(
                            text = if (isOverlayActive) "Stop Floating Overlay" else "Launch Floating Overlay",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Method 3: Wireless Debugging / Mantis Buddy Pairing
        item {
            CyberCard(backgroundColor = SurfaceDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "3. Wireless Debugging (Mantis Buddy)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Android 11+ Split-Screen Wi-Fi Pairing",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        StatusBadge(
                            text = if (isPairingSimulated) "PAIRED" else "READY",
                            isActive = isPairingSimulated,
                            activeColor = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "For competitive e-sports players: activates the high-speed input daemon with direct touch pipe dispatch (<1.0ms latency).",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pairingPort,
                            onValueChange = { pairingPort = it },
                            label = { Text("Port (5 digits)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = pairingCode,
                            onValueChange = { pairingCode = it },
                            label = { Text("Pairing Code") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { isPairingSimulated = !isPairingSimulated },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPairingSimulated) SurfaceDarkElevated else NeonOrange
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPairingSimulated) "Disconnect Buddy Daemon" else "Simulate Wi-Fi Pair & Connect",
                            color = if (isPairingSimulated) CyberCyan else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
