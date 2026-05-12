package com.emonotion.app.data.local.dao

import androidx.room.*
import com.emonotion.app.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с профилем пользователя
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUserProfile(userId: String = "current_user"): UserProfileEntity?
    
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfileFlow(userId: String = "current_user"): Flow<UserProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserProfile(profile: UserProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String = "current_user")
    
    @Query("DELETE FROM user_profile")
    suspend fun deleteAllUserProfiles()
}
