package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.IdeaDao
import com.example.data.local.IdeaIntelligenceDatabase
import com.example.data.model.Idea
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
class PortfolioSearchAndDeleteTest {

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
    fun testDeleteIdeaFromDatabase() = runBlocking {
        val idea1 = Idea(id = "idea-1", name = "AI Medical Coder", oneLineThesis = "Billing AI", description = "Desc", category = "Healthcare", tag = "Healthcare")
        val idea2 = Idea(id = "idea-2", name = "Home Repair Escrow", oneLineThesis = "Trade Escrow", description = "Desc", category = "Fintech", tag = "Fintech")

        ideaDao.insertIdea(idea1)
        ideaDao.insertIdea(idea2)
        assertEquals(2, ideaDao.countIdeas())

        // Delete idea1
        ideaDao.deleteIdeaById("idea-1")

        val remaining = ideaDao.getAllIdeas().first()
        assertEquals(1, remaining.size)
        assertEquals("idea-2", remaining.first().id)
        assertNull(ideaDao.getIdeaByIdOnce("idea-1"))
    }

    @Test
    fun testFilterByTitleAndTag() = runBlocking {
        val list = listOf(
            Idea(id = "1", name = "MediClaim AI", oneLineThesis = "Medical claims", description = "Desc", category = "Healthcare", tag = "Urgent"),
            Idea(id = "2", name = "TradeTrust Escrow", oneLineThesis = "Contractor bids", description = "Desc", category = "Marketplace", tag = "Austin"),
            Idea(id = "3", name = "RemitClear", oneLineThesis = "FX fee audits", description = "Desc", category = "Fintech", tag = "Diaspora")
        )

        // Filter by title "MediClaim"
        val queryTitle = "mediclaim"
        val filteredByTitle = list.filter {
            it.name.contains(queryTitle, ignoreCase = true) || it.tag.contains(queryTitle, ignoreCase = true)
        }
        assertEquals(1, filteredByTitle.size)
        assertEquals("1", filteredByTitle.first().id)

        // Filter by tag "Austin" or "#Austin"
        val queryTag = "#austin".removePrefix("#")
        val filteredByTag = list.filter {
            it.name.contains(queryTag, ignoreCase = true) || it.tag.contains(queryTag, ignoreCase = true)
        }
        assertEquals(1, filteredByTag.size)
        assertEquals("2", filteredByTag.first().id)
    }

    @Test
    fun testPriorityFilteringAndSorting() = runBlocking {
        val list = listOf(
            Idea(id = "1", name = "MediClaim AI", oneLineThesis = "Claims", description = "Desc", category = "Tech", tag = "B2B", priority = "High"),
            Idea(id = "2", name = "TradeTrust Escrow", oneLineThesis = "Trades", description = "Desc", category = "Tech", tag = "B2B", priority = "Medium"),
            Idea(id = "3", name = "SiteForge Trades", oneLineThesis = "Sites", description = "Desc", category = "Tech", tag = "B2B", priority = "Low"),
            Idea(id = "4", name = "VeriDoc Auto", oneLineThesis = "Licenses", description = "Desc", category = "Tech", tag = "B2B", priority = "High")
        )

        // Filter High Priority
        val highPriority = list.filter { it.priority.equals("High", ignoreCase = true) }
        assertEquals(2, highPriority.size)
        assertTrue(highPriority.any { it.id == "1" })
        assertTrue(highPriority.any { it.id == "4" })

        // Sort by Priority descending: High (3) > Medium (2) > Low (1)
        fun weight(p: String): Int = when (p.lowercase()) {
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 2
        }

        val sortedByPriority = list.sortedByDescending { weight(it.priority) }
        assertEquals("High", sortedByPriority[0].priority)
        assertEquals("High", sortedByPriority[1].priority)
        assertEquals("Medium", sortedByPriority[2].priority)
        assertEquals("Low", sortedByPriority[3].priority)
    }

    @Test
    fun testPrioritySavedInDatabase() = runBlocking {
        val idea = Idea(
            id = "test-priority-1",
            name = "Test Opportunity",
            oneLineThesis = "Thesis",
            description = "Detailed notes",
            category = "AI",
            tag = "MVP",
            priority = "High"
        )

        ideaDao.insertIdea(idea)
        val loaded = ideaDao.getIdeaByIdOnce("test-priority-1")
        assertNotNull(loaded)
        assertEquals("High", loaded?.priority)
        assertEquals("MVP", loaded?.tag)

        // Update priority to Medium
        val updated = loaded!!.copy(priority = "Medium")
        ideaDao.updateIdea(updated)
        val reloaded = ideaDao.getIdeaByIdOnce("test-priority-1")
        assertEquals("Medium", reloaded?.priority)
    }

    @Test
    fun testUndoDeleteRestoresIdeaInDatabase() = runBlocking {
        val idea = Idea(
            id = "undo-idea-1",
            name = "Venture for Undo Test",
            oneLineThesis = "Restore validation",
            description = "Detailed notes",
            category = "SaaS",
            tag = "B2B",
            priority = "High"
        )

        ideaDao.insertIdea(idea)
        assertEquals(1, ideaDao.countIdeas())

        // Simulate delete
        ideaDao.deleteIdeaById("undo-idea-1")
        assertNull(ideaDao.getIdeaByIdOnce("undo-idea-1"))
        assertEquals(0, ideaDao.countIdeas())

        // Simulate UNDO restore (insertIdea)
        ideaDao.insertIdea(idea)
        val restored = ideaDao.getIdeaByIdOnce("undo-idea-1")
        assertNotNull(restored)
        assertEquals("Venture for Undo Test", restored?.name)
        assertEquals(1, ideaDao.countIdeas())
    }
}
