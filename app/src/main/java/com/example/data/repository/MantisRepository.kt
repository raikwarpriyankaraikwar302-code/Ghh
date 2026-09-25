package com.example.data.repository

import com.example.data.local.MantisDao
import com.example.data.model.GameProfile
import com.example.data.model.KeyMapping
import com.example.data.model.TouchGestureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MantisRepository(private val dao: MantisDao) {

    val allProfiles: Flow<List<GameProfile>> = dao.getAllProfiles()

    fun getProfile(id: Long): Flow<GameProfile?> = dao.getProfileById(id)

    fun getMappingsForProfile(profileId: Long): Flow<List<KeyMapping>> =
        dao.getMappingsForProfile(profileId)

    suspend fun saveProfile(profile: GameProfile): Long {
        return if (profile.id == 0L) {
            val newId = dao.insertProfile(profile)
            seedDefaultMappingsForProfile(newId, profile.genre)
            newId
        } else {
            dao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun deleteProfile(id: Long) {
        dao.deleteMappingsForProfile(id)
        dao.deleteProfile(id)
    }

    suspend fun saveMapping(mapping: KeyMapping): Long {
        return if (mapping.id == 0L) {
            dao.insertMapping(mapping)
        } else {
            dao.updateMapping(mapping)
            mapping.id
        }
    }

    suspend fun deleteMapping(id: Long) = dao.deleteMapping(id)

    suspend fun resetMappingsToDefault(profileId: Long, genre: String) {
        dao.deleteMappingsForProfile(profileId)
        seedDefaultMappingsForProfile(profileId, genre)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = allProfiles.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val initialGames = listOf(
                GameProfile(
                    title = "Call of Duty: Mobile",
                    packageName = "com.activision.callofduty.shooter",
                    genre = "FPS",
                    isFavorite = true,
                    targetFps = 120,
                    pollingRateHz = 1000,
                    bannerColorHex = "#00E5FF",
                    totalPlayMinutes = 142
                ),
                GameProfile(
                    title = "Genshin Impact",
                    packageName = "com.miHoYo.GenshinImpact",
                    genre = "Action RPG",
                    isFavorite = true,
                    targetFps = 60,
                    pollingRateHz = 500,
                    bannerColorHex = "#A855F7",
                    totalPlayMinutes = 320
                ),
                GameProfile(
                    title = "PUBG Mobile / BGMI",
                    packageName = "com.tencent.ig",
                    genre = "Battle Royale",
                    isFavorite = false,
                    targetFps = 90,
                    pollingRateHz = 500,
                    bannerColorHex = "#F97316",
                    totalPlayMinutes = 88
                ),
                GameProfile(
                    title = "Apex Legends Mobile",
                    packageName = "com.ea.gp.apexlegendsmobilefps",
                    genre = "FPS",
                    isFavorite = false,
                    targetFps = 120,
                    pollingRateHz = 1000,
                    bannerColorHex = "#EF4444",
                    totalPlayMinutes = 64
                ),
                GameProfile(
                    title = "Brawl Stars",
                    packageName = "com.supercell.brawlstars",
                    genre = "MOBA",
                    isFavorite = false,
                    targetFps = 120,
                    pollingRateHz = 500,
                    bannerColorHex = "#FBBF24",
                    totalPlayMinutes = 45
                ),
                GameProfile(
                    title = "CarX Street",
                    packageName = "com.carxtech.sr",
                    genre = "Racing",
                    isFavorite = false,
                    targetFps = 60,
                    pollingRateHz = 250,
                    bannerColorHex = "#10B981",
                    totalPlayMinutes = 30
                )
            )

            for (game in initialGames) {
                val profileId = dao.insertProfile(game)
                seedDefaultMappingsForProfile(profileId, game.genre)
            }
        }
    }

    private suspend fun seedDefaultMappingsForProfile(profileId: Long, genre: String) {
        val mappings = when (genre) {
            "MOBA" -> listOf(
                KeyMapping(profileId = profileId, buttonKey = "LEFT_STICK", displayLabel = "Move Stick", touchXPercent = 0.20f, touchYPercent = 0.75f, radiusDp = 60f, gestureType = TouchGestureType.ANALOG_STICK),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_A", displayLabel = "Normal Attack", touchXPercent = 0.85f, touchYPercent = 0.78f, radiusDp = 40f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "RT", displayLabel = "Super Ability", touchXPercent = 0.72f, touchYPercent = 0.65f, radiusDp = 38f, gestureType = TouchGestureType.SMART_AIM),
                KeyMapping(profileId = profileId, buttonKey = "RB", displayLabel = "Dash Swipe", touchXPercent = 0.80f, touchYPercent = 0.58f, radiusDp = 34f, gestureType = TouchGestureType.SWIPE, swipeAngleDeg = 0f, swipeDistanceDp = 70f),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_Y", displayLabel = "Recall (Hold)", touchXPercent = 0.90f, touchYPercent = 0.40f, radiusDp = 28f, gestureType = TouchGestureType.HOLD, holdDurationMs = 500L)
            )
            "Racing" -> listOf(
                KeyMapping(profileId = profileId, buttonKey = "RT", displayLabel = "Throttle Gas", touchXPercent = 0.88f, touchYPercent = 0.75f, radiusDp = 48f, gestureType = TouchGestureType.HOLD),
                KeyMapping(profileId = profileId, buttonKey = "LT", displayLabel = "Foot Brake", touchXPercent = 0.12f, touchYPercent = 0.75f, radiusDp = 48f, gestureType = TouchGestureType.HOLD),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_A", displayLabel = "Handbrake Drift", touchXPercent = 0.75f, touchYPercent = 0.80f, radiusDp = 42f, gestureType = TouchGestureType.HOLD),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_B", displayLabel = "Nitrous NOS", touchXPercent = 0.82f, touchYPercent = 0.58f, radiusDp = 38f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "LEFT_STICK", displayLabel = "Steering Wheel", touchXPercent = 0.20f, touchYPercent = 0.50f, radiusDp = 64f, gestureType = TouchGestureType.ANALOG_STICK),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_Y", displayLabel = "Rear View (Hold)", touchXPercent = 0.92f, touchYPercent = 0.20f, radiusDp = 30f, gestureType = TouchGestureType.HOLD)
            )
            else -> listOf(
                // Standard FPS & Action RPG layout
                KeyMapping(profileId = profileId, buttonKey = "LEFT_STICK", displayLabel = "Move WASD", touchXPercent = 0.18f, touchYPercent = 0.72f, radiusDp = 56f, gestureType = TouchGestureType.ANALOG_STICK),
                KeyMapping(profileId = profileId, buttonKey = "RIGHT_STICK", displayLabel = "Look & Aim", touchXPercent = 0.75f, touchYPercent = 0.54f, radiusDp = 68f, gestureType = TouchGestureType.CAMERA_LOOK, sensitivityX = 1.2f, sensitivityY = 1.0f),
                KeyMapping(profileId = profileId, buttonKey = "RT", displayLabel = "Fire Weapon", touchXPercent = 0.86f, touchYPercent = 0.72f, radiusDp = 40f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "LT", displayLabel = "ADS Scope", touchXPercent = 0.88f, touchYPercent = 0.44f, radiusDp = 36f, gestureType = TouchGestureType.HOLD),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_A", displayLabel = "Jump", touchXPercent = 0.92f, touchYPercent = 0.82f, radiusDp = 34f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_B", displayLabel = "Slide Swipe", touchXPercent = 0.79f, touchYPercent = 0.88f, radiusDp = 34f, gestureType = TouchGestureType.SWIPE, swipeAngleDeg = 90f, swipeDistanceDp = 60f),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_X", displayLabel = "Reload", touchXPercent = 0.73f, touchYPercent = 0.76f, radiusDp = 34f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_Y", displayLabel = "Weapon Swap Swipe", touchXPercent = 0.82f, touchYPercent = 0.62f, radiusDp = 34f, gestureType = TouchGestureType.SWIPE, swipeAngleDeg = 0f, swipeDistanceDp = 50f),
                KeyMapping(profileId = profileId, buttonKey = "RB", displayLabel = "Grenade", touchXPercent = 0.93f, touchYPercent = 0.58f, radiusDp = 32f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "LB", displayLabel = "Tactical Ability", touchXPercent = 0.66f, touchYPercent = 0.88f, radiusDp = 32f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_THUMBL", displayLabel = "Sprint Lock Swipe", touchXPercent = 0.18f, touchYPercent = 0.54f, radiusDp = 30f, gestureType = TouchGestureType.SWIPE, swipeAngleDeg = 270f, swipeDistanceDp = 70f),
                KeyMapping(profileId = profileId, buttonKey = "BUTTON_THUMBR", displayLabel = "Melee Attack", touchXPercent = 0.68f, touchYPercent = 0.66f, radiusDp = 30f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "DPAD_UP", displayLabel = "Backpack", touchXPercent = 0.08f, touchYPercent = 0.88f, radiusDp = 28f, gestureType = TouchGestureType.TAP),
                KeyMapping(profileId = profileId, buttonKey = "DPAD_DOWN", displayLabel = "Heal Medkit", touchXPercent = 0.28f, touchYPercent = 0.92f, radiusDp = 28f, gestureType = TouchGestureType.HOLD, holdDurationMs = 400L),
                KeyMapping(profileId = profileId, buttonKey = "DPAD_LEFT", displayLabel = "Map Expand", touchXPercent = 0.92f, touchYPercent = 0.12f, radiusDp = 28f, gestureType = TouchGestureType.TAP)
            )
        }
        dao.insertMappings(mappings)
    }
}
