package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    @TypeConverter
    fun fromEpistemicClaims(value: List<EpistemicClaim>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { claim ->
            val obj = JSONObject()
            obj.put("claim", claim.claim)
            obj.put("type", claim.type.name)
            obj.put("sourceOrEvidence", claim.sourceOrEvidence)
            obj.put("confidence", claim.confidence)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toEpistemicClaims(value: String?): List<EpistemicClaim> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<EpistemicClaim>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val typeStr = obj.optString("type", "FACT")
            val type = try { EpistemicType.valueOf(typeStr) } catch (_: Exception) { EpistemicType.FACT }
            list.add(
                EpistemicClaim(
                    claim = obj.optString("claim", ""),
                    type = type,
                    sourceOrEvidence = obj.optString("sourceOrEvidence", ""),
                    confidence = obj.optString("confidence", "Medium")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromRequirements(value: List<RequirementItem>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { item ->
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("necessity", item.necessity)
            obj.put("difficulty", item.difficulty)
            obj.put("status", item.status)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toRequirements(value: String?): List<RequirementItem> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<RequirementItem>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                RequirementItem(
                    title = obj.optString("title", ""),
                    category = obj.optString("category", "Functional"),
                    necessity = obj.optString("necessity", "MUST"),
                    difficulty = obj.optString("difficulty", "Medium"),
                    status = obj.optString("status", "Open")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromRisks(value: List<RiskItem>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { item ->
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("severity", item.severity)
            obj.put("likelihood", item.likelihood)
            obj.put("mitigation", item.mitigation)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toRisks(value: String?): List<RiskItem> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<RiskItem>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                RiskItem(
                    title = obj.optString("title", ""),
                    category = obj.optString("category", "Market"),
                    severity = obj.optString("severity", "Medium"),
                    likelihood = obj.optString("likelihood", "Medium"),
                    mitigation = obj.optString("mitigation", "")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromCompetitors(value: List<CompetitorItem>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { item ->
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("type", item.type)
            obj.put("positioning", item.positioning)
            obj.put("pricing", item.pricing)
            obj.put("strengths", item.strengths)
            obj.put("weaknesses", item.weaknesses)
            obj.put("threatLevel", item.threatLevel)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toCompetitors(value: String?): List<CompetitorItem> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<CompetitorItem>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                CompetitorItem(
                    name = obj.optString("name", ""),
                    type = obj.optString("type", "Direct"),
                    positioning = obj.optString("positioning", ""),
                    pricing = obj.optString("pricing", ""),
                    strengths = obj.optString("strengths", ""),
                    weaknesses = obj.optString("weaknesses", ""),
                    threatLevel = obj.optString("threatLevel", "Medium")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromMarketGaps(value: List<MarketGap>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { gap ->
            val obj = JSONObject()
            obj.put("title", gap.title)
            obj.put("description", gap.description)
            obj.put("defensibility", gap.defensibility)
            obj.put("validationScore", gap.validationScore)
            obj.put("urgency", gap.urgency)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toMarketGaps(value: String?): List<MarketGap> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<MarketGap>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                MarketGap(
                    title = obj.optString("title", ""),
                    description = obj.optString("description", ""),
                    defensibility = obj.optString("defensibility", "Medium"),
                    validationScore = obj.optString("validationScore", "High"),
                    urgency = obj.optString("urgency", "Medium")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromExperiments(value: List<ValidationExperiment>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { exp ->
            val obj = JSONObject()
            obj.put("id", exp.id)
            obj.put("assumption", exp.assumption)
            obj.put("experimentType", exp.experimentType)
            obj.put("successCriteria", exp.successCriteria)
            obj.put("status", exp.status)
            obj.put("notes", exp.notes)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toExperiments(value: String?): List<ValidationExperiment> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ValidationExperiment>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ValidationExperiment(
                    id = obj.optString("id", ""),
                    assumption = obj.optString("assumption", ""),
                    experimentType = obj.optString("experimentType", ""),
                    successCriteria = obj.optString("successCriteria", ""),
                    status = obj.optString("status", "PENDING"),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromActionItems(value: List<ActionItem>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("task", item.task)
            obj.put("timeline", item.timeline)
            obj.put("priority", item.priority)
            obj.put("effort", item.effort)
            obj.put("completed", item.completed)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toActionItems(value: String?): List<ActionItem> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ActionItem>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ActionItem(
                    id = obj.optString("id", ""),
                    task = obj.optString("task", ""),
                    timeline = obj.optString("timeline", "7 Days"),
                    priority = obj.optString("priority", "P1"),
                    effort = obj.optString("effort", "Medium"),
                    completed = obj.optBoolean("completed", false)
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromSmartReminders(value: List<SmartReminder>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { rem ->
            val obj = JSONObject()
            obj.put("id", rem.id)
            obj.put("title", rem.title)
            obj.put("type", rem.type)
            obj.put("triggerCondition", rem.triggerCondition)
            obj.put("dueDate", rem.dueDate)
            obj.put("isTriggered", rem.isTriggered)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toSmartReminders(value: String?): List<SmartReminder> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<SmartReminder>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SmartReminder(
                    id = obj.optString("id", ""),
                    title = obj.optString("title", ""),
                    type = obj.optString("type", ""),
                    triggerCondition = obj.optString("triggerCondition", ""),
                    dueDate = obj.optString("dueDate", ""),
                    isTriggered = obj.optBoolean("isTriggered", false)
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromScoreHistory(value: List<ScoreHistoryEntry>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { entry ->
            val obj = JSONObject()
            obj.put("timestamp", entry.timestamp)
            obj.put("dateLabel", entry.dateLabel)
            obj.put("opportunityScore", entry.opportunityScore)
            obj.put("timingScore", entry.timingScore)
            obj.put("feasibilityScore", entry.feasibilityScore)
            obj.put("reason", entry.reason)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toScoreHistory(value: String?): List<ScoreHistoryEntry> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ScoreHistoryEntry>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ScoreHistoryEntry(
                    timestamp = obj.optLong("timestamp", 0L),
                    dateLabel = obj.optString("dateLabel", ""),
                    opportunityScore = obj.optInt("opportunityScore", 75),
                    timingScore = obj.optInt("timingScore", 75),
                    feasibilityScore = obj.optInt("feasibilityScore", 75),
                    reason = obj.optString("reason", "")
                )
            )
        }
        return list
    }

    @TypeConverter
    fun fromChatHistory(value: List<ChatMessage>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { msg ->
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("isUser", msg.isUser)
            obj.put("text", msg.text)
            obj.put("timestamp", msg.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toChatHistory(value: String?): List<ChatMessage> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ChatMessage>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ChatMessage(
                    id = obj.optString("id", ""),
                    isUser = obj.optBoolean("isUser", false),
                    text = obj.optString("text", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }
}
