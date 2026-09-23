package com.example.data.model

enum class SuggestionType(val iconEmoji: String) {
    DEADLINE_WARNING("⚠️"),
    OPTIMAL_FOCUS_BLOCK("🎯"),
    RESCHEDULE_LOW_PRIORITY("🔄"),
    QUICK_WIN("⚡"),
    NOTE_TO_ACTION("📝")
}

data class SmartSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: SuggestionType,
    val actionText: String,
    val relatedTaskId: Long? = null,
    val suggestedSlot: String? = null,
    val urgencyScore: Int = 85
)
