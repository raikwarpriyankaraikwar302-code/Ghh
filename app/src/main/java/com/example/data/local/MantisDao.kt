package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GameProfile
import com.example.data.model.KeyMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface MantisDao {

    // Game Profiles
    @Query("SELECT * FROM game_profiles ORDER BY lastPlayedTimestamp DESC")
    fun getAllProfiles(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<GameProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GameProfile): Long

    @Update
    suspend fun updateProfile(profile: GameProfile)

    @Query("DELETE FROM game_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    // Key Mappings
    @Query("SELECT * FROM key_mappings WHERE profileId = :profileId")
    fun getMappingsForProfile(profileId: Long): Flow<List<KeyMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: KeyMapping): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<KeyMapping>)

    @Update
    suspend fun updateMapping(mapping: KeyMapping)

    @Query("DELETE FROM key_mappings WHERE id = :id")
    suspend fun deleteMapping(id: Long)

    @Query("DELETE FROM key_mappings WHERE profileId = :profileId")
    suspend fun deleteMappingsForProfile(profileId: Long)
}
