package com.example.data.local

import androidx.room.*
import com.example.data.model.Idea
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY updatedAt DESC")
    fun getAllIdeas(): Flow<List<Idea>>

    @Query("SELECT * FROM ideas")
    suspend fun getAllIdeasOnce(): List<Idea>

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    fun getIdeaById(id: String): Flow<Idea?>

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    suspend fun getIdeaByIdOnce(id: String): Idea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: Idea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdeas(ideas: List<Idea>)

    @Update
    suspend fun updateIdea(idea: Idea)

    @Delete
    suspend fun deleteIdea(idea: Idea)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteIdeaById(id: String)

    @Query("DELETE FROM ideas")
    suspend fun clearAllIdeas()

    @Query("SELECT COUNT(*) FROM ideas")
    suspend fun countIdeas(): Int

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}
