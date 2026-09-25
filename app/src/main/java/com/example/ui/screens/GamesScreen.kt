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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.GameProfile
import com.example.ui.MantisViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TelemetryStat
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
fun GamesScreen(
    viewModel: MantisViewModel,
    onNavigateToKeymapper: (GameProfile) -> Unit,
    onNavigateToTestArena: (GameProfile) -> Unit
) {
    val context = LocalContext.current
    val profiles by viewModel.allProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGenreFilter by remember { mutableStateOf("All") }

    val genres = listOf("All", "FPS", "Battle Royale", "Action RPG", "MOBA", "Racing")

    val filteredProfiles = remember(profiles, selectedGenreFilter) {
        if (selectedGenreFilter == "All") profiles
        else profiles.filter { it.genre.equals(selectedGenreFilter, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0E17))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controller & Engine Quick Status Card
            item {
                CyberCard(
                    borderColor = if (controllerState.isConnected) NeonGreen else CyberCyan.copy(alpha = 0.4f),
                    backgroundColor = SurfaceDarkElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (controllerState.isConnected) NeonGreen.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Gamepad Status",
                                        tint = if (controllerState.isConnected) NeonGreen else CyberCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = controllerState.controllerName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (controllerState.isConnected) "Hardware link active • Ready to map" else "Connect via Bluetooth, USB-C OTG or use Simulator",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            StatusBadge(
                                text = if (controllerState.isConnected) "ONLINE" else "STANDBY",
                                isActive = controllerState.isConnected
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TelemetryStat(
                                label = "Touch Latency",
                                value = String.format("%.1f", controllerState.latencyMs),
                                unit = "ms",
                                accentColor = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryStat(
                                label = "Engine Polling",
                                value = "${controllerState.packetsPerSec}",
                                unit = "Hz",
                                accentColor = ElectricViolet,
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryStat(
                                label = "Overlay HUD",
                                value = if (isOverlayActive) "ON" else "OFF",
                                unit = "",
                                accentColor = if (isOverlayActive) NeonGreen else TextSecondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.toggleOverlay(context) }
                            )
                        }
                    }
                }
            }

            // Genre Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        val isSelected = selectedGenreFilter == genre
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CyberCyan else SurfaceDarkElevated)
                                .border(1.dp, if (isSelected) CyberCyan else SurfaceBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedGenreFilter = genre }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Games List
            items(filteredProfiles, key = { it.id }) { profile ->
                val isSelected = selectedProfile?.id == profile.id
                val bannerColor = try {
                    Color(android.graphics.Color.parseColor(profile.bannerColorHex))
                } catch (_: Exception) {
                    CyberCyan
                }

                CyberCard(
                    borderColor = if (isSelected) bannerColor else SurfaceBorder,
                    backgroundColor = SurfaceDark,
                    onClick = { viewModel.selectProfile(profile) },
                    modifier = Modifier.testTag("game_card_${profile.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bannerColor.copy(alpha = 0.2f))
                                    .border(1.dp, bannerColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.title.take(2).uppercase(),
                                    color = bannerColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bannerColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = profile.genre,
                                            color = bannerColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = profile.packageName,
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteProfile(profile.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Game Profile",
                                    tint = TextTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge(
                                    text = "${profile.targetFps} FPS",
                                    isActive = true,
                                    activeColor = NeonOrange
                                )
                                StatusBadge(
                                    text = "${profile.pollingRateHz} Hz Rate",
                                    isActive = true,
                                    activeColor = ElectricViolet
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.selectProfile(profile)
                                        onNavigateToKeymapper(profile)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("edit_keymap_${profile.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Edit Keymap",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Map", color = TextPrimary, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.selectProfile(profile)
                                        onNavigateToTestArena(profile)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("launch_test_${profile.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Launch Game",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Launch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Game
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_game_fab"),
            containerColor = CyberCyan,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Game Profile")
        }

        // Add Game Modal
        if (showAddDialog) {
            AddGameDialog(
                onDismiss = { showAddDialog = false },
                onAddGame = { title, pkg, genre ->
                    viewModel.addGameProfile(title, pkg, genre)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onAddGame: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("FPS") }

    val presets = listOf(
        Triple("Minecraft", "com.mojang.minecraftpe", "Action RPG"),
        Triple("Free Fire MAX", "com.dts.freefiremax", "Battle Royale"),
        Triple("Shadowgun Legends", "com.madfingergames.legends", "FPS"),
        Triple("League of Legends: Wild Rift", "com.riotgames.league.wildrift", "MOBA"),
        Triple("Asphalt 9: Legends", "com.gameloft.android.ANMP.GloftA9HM", "Racing")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("Add Game Profile", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Quick Popular Presets:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presets) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDarkElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    title = preset.first
                                    packageName = preset.second
                                    selectedGenre = preset.third
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(preset.first, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Game Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_game_title_input")
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. com.game.shooter)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_game_package_input")
                )

                Text("Game Genre / Mapping Template:", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("FPS", "Action RPG", "MOBA", "Racing").forEach { genre ->
                        val isSel = selectedGenre == genre
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                .clickable { selectedGenre = genre }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = genre,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddGame(title, packageName, selectedGenre)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier.testTag("confirm_add_game")
            ) {
                Text("Add Profile", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
