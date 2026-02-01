package com.edward.studytracker.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("study_tracker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_PROJECT_ID = "current_project_id"
    }

    var currentProjectId: Int
        get() = prefs.getInt(KEY_CURRENT_PROJECT_ID, -1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_PROJECT_ID, value).apply()
}