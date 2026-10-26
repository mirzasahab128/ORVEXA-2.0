package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.IdeaDao
import com.example.data.local.IdeaIntelligenceDatabase
import com.example.data.model.Idea
import com.example.data.model.IdeaStatus
import com.example.data.model.Recommendation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IdeaDatabaseTest {

    private lateinit var database: IdeaIntelligenceDatabase
    private lateinit var ideaDao: IdeaDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, IdeaIntelligenceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        ideaDao = database.ideaDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveIdea() = runBlocking {
        val now = System.currentTimeMillis()
        val testIdea = Idea(
            id = "test-idea-1",
            name = "Smart Logistics Router",
            oneLineThesis = "Autonomous routing predicting freight penalties",
            description = "AI dispatch software saving hours for truck fleet operators",
            category = "Logistics",
            tag = "MVP",
            categories = listOf("Logistics", "AI", "B2B"),
            status = IdeaStatus.DRAFT,
            opportunityScore = 82,
            timingScore = 79,
            feasibilityScore = 75,
            recommendation = Recommendation.VALIDATE_FIRST,
            problemStatement = "Dispatchers lose 15% revenue on route delays",
            createdAt = now
        )

        ideaDao.insertIdea(testIdea)

        val retrieved = ideaDao.getIdeaByIdOnce("test-idea-1")
        assertNotNull("Retrieved idea should not be null", retrieved)
        assertEquals("Smart Logistics Router", retrieved?.name)
        assertEquals("AI dispatch software saving hours for truck fleet operators", retrieved?.description)
        assertEquals("Logistics", retrieved?.category)
        assertEquals("MVP", retrieved?.tag)
        assertEquals(now, retrieved?.createdAt)
        assertEquals(Recommendation.VALIDATE_FIRST, retrieved?.recommendation)
        assertEquals(82, retrieved?.opportunityScore)
        assertEquals(listOf("Logistics", "AI", "B2B"), retrieved?.categories)
    }

    @Test
    fun testFlowEmissionAndCount() = runBlocking {
        assertEquals(0, ideaDao.countIdeas())

        val ideaA = Idea(
            id = "idea-a",
            name = "Idea A",
            oneLineThesis = "Thesis A",
            description = "Description A",
            category = "SaaS",
            tag = "B2B",
            categories = listOf("SaaS")
        )
        val ideaB = Idea(
            id = "idea-b",
            name = "Idea B",
            oneLineThesis = "Thesis B",
            description = "Description B",
            category = "Healthcare",
            tag = "B2B",
            categories = listOf("Healthcare")
        )

        ideaDao.insertIdea(ideaA)
        ideaDao.insertIdea(ideaB)

        assertEquals(2, ideaDao.countIdeas())

        val allIdeas = ideaDao.getAllIdeas().first()
        assertEquals(2, allIdeas.size)
        assertTrue(allIdeas.any { it.id == "idea-a" })
        assertTrue(allIdeas.any { it.id == "idea-b" })
    }

    @Test
    fun testUpdateAndDeleteIdea() = runBlocking {
        val idea = Idea(
            id = "idea-edit",
            name = "Original Name",
            oneLineThesis = "Thesis",
            description = "Description",
            category = "General",
            tag = "B2B",
            categories = listOf("General")
        )
        ideaDao.insertIdea(idea)

        val updated = idea.copy(name = "Updated Name", opportunityScore = 95)
        ideaDao.updateIdea(updated)

        val retrieved = ideaDao.getIdeaByIdOnce("idea-edit")
        assertEquals("Updated Name", retrieved?.name)
        assertEquals(95, retrieved?.opportunityScore)

        ideaDao.deleteIdeaById("idea-edit")
        assertNull(ideaDao.getIdeaByIdOnce("idea-edit"))
        assertEquals(0, ideaDao.countIdeas())
    }
}
