package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProfile
import com.example.ui.components.OverlayHudComponent
import com.example.ui.components.StatusBadge
import com.example.ui.screens.ActivationScreen
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.KeymapperScreen
import com.example.ui.screens.TestArenaScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MantisScreenTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GAMES("Games", Icons.Default.Games),
    KEYMAPPER("Keymapper", Icons.Default.TouchApp),
    CALIBRATION("Calibration", Icons.Default.Tune),
    TEST_ARENA("Test Arena", Icons.Default.PlayCircle),
    ACTIVATION("Activation", Icons.Default.VerifiedUser)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantisApp(viewModel: MantisViewModel) {
    var currentTab by remember { mutableStateOf(MantisScreenTab.GAMES) }
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val hudConfig by viewModel.hudConfig.collectAsState()
    val recentTriggers by viewModel.recentTriggers.collectAsState()
    val activeTouches by viewModel.activeTouches.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0E17),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyan)
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MANTIS",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PRO",
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "LOW LATENCY TOUCH MAPPER",
                                color = ElectricViolet,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // HUD Overlay Toggle Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (hudConfig.isVisible) CyberCyan.copy(alpha = 0.2f) else SurfaceDarkElevated)
                            .border(1.dp, if (hudConfig.isVisible) CyberCyan else SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleHudVisibility() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("top_bar_hud_toggle"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Toggle Overlay HUD",
                            tint = if (hudConfig.isVisible) CyberCyan else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "HUD",
                            color = if (hudConfig.isVisible) CyberCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Controller mini indicator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (controllerState.isConnected) NeonGreen else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${String.format("%.1f", controllerState.latencyMs)} ms",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(BorderStroke(1.dp, SurfaceBorder), shape = RoundedCornerShape(0.dp))
                    .testTag("mantis_bottom_nav")
            ) {
                MantisScreenTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyberCyan else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MantisScreenTab.GAMES -> {
                    GamesScreen(
                        viewModel = viewModel,
                        onNavigateToKeymapper = { profile ->
                            currentTab = MantisScreenTab.KEYMAPPER
                        },
                        onNavigateToTestArena = { profile ->
                            currentTab = MantisScreenTab.TEST_ARENA
                        }
                    )
                }
                MantisScreenTab.KEYMAPPER -> {
                    KeymapperScreen(
                        viewModel = viewModel,
                        onNavigateToTestArena = { profile ->
                            currentTab = MantisScreenTab.TEST_ARENA
                        }
                    )
                }
                MantisScreenTab.CALIBRATION -> {
                    CalibrationScreen(viewModel = viewModel)
                }
                MantisScreenTab.TEST_ARENA -> {
                    TestArenaScreen(
                        viewModel = viewModel,
                        profile = selectedProfile,
                        onBack = { currentTab = MantisScreenTab.GAMES }
                    )
                }
                MantisScreenTab.ACTIVATION -> {
                    ActivationScreen(viewModel = viewModel)
                }
            }

            // Floating Overlay HUD Component
            OverlayHudComponent(
                hudConfig = hudConfig,
                controllerState = controllerState,
                activeTouches = activeTouches,
                recentTriggers = recentTriggers,
                onModeChange = { viewModel.setHudMode(it) },
                onOpacityChange = { viewModel.setHudOpacity(it) },
                onMinimizeToggle = { viewModel.setHudMinimized(it) },
                onClose = { viewModel.toggleHudVisibility(false) },
                onTriggerTest = { name, key, gesture ->
                    viewModel.triggerManualAction(name, key, gesture)
                },
                onClearHistory = { viewModel.clearTriggerHistory() }
            )
        }
    }
}
