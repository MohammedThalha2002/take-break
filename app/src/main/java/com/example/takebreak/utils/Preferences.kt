package com.example.takebreak.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal object Preferences {
    private const val PreferenceName = "take.break"
    private const val PreferenceKey = "blocked_app_list"

    private var sharedPreferences: SharedPreferences? = null

    @Synchronized
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        }
    }

    suspend fun addApp(app: String) {
        withContext(coroutineContext) {
            coroutineScope {
                sharedPreferences?.let {
                    val currentApps = it.getString(PreferenceKey, null)?.split(",")?.toMutableList() ?: mutableListOf()
                    if (app !in currentApps) {
                        currentApps.add(app)
                        it.edit(commit = true) {
                            putString(PreferenceKey, currentApps.joinToString(","))
                        }
                    }
                }
            }
        }
    }

    suspend fun removeApp(app: String) {
        withContext(coroutineContext) {
            coroutineScope {
                sharedPreferences?.let {
                    val currentApps = it.getString(PreferenceKey, null)?.split(",")?.toMutableList() ?: mutableListOf()
                    if (app in currentApps) {
                        currentApps.remove(app)
                        it.edit(commit = true) {
                            putString(PreferenceKey, currentApps.joinToString(","))
                        }
                    }
                }
            }
        }
    }

    fun getBlockedApps(): List<String> {
        return sharedPreferences?.getString(PreferenceKey, null)
            ?.split(",") ?: listOf()
    }

    fun isAppBlocked(app: String): Boolean {
        return getBlockedApps().contains(app)
    }

    suspend fun clearBlockedApps() {
        withContext(coroutineContext) {
            coroutineScope {
                sharedPreferences?.edit(commit = true) {
                    remove(PreferenceKey)
                }
            }
        }
    }
}