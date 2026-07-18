package com.example.takebreak

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.takebreak.utils.Preferences

class AppMonitorAccessibilityService : AccessibilityService() {
    private var currentForegroundApp: String? = null
    private var lastEventTime = 0L
    private val eventDebounceMs = 500L // Prevent duplicate events

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (InterceptControl.isTemporarilyAllowed) return

        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handleWindowEvent(event)
        }
    }

    private fun handleWindowEvent(event: AccessibilityEvent) {
        val currentTime = System.currentTimeMillis()

        // Debounce rapid events
        if (currentTime - lastEventTime < eventDebounceMs) {
            return
        }
        lastEventTime = currentTime

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return

        // Skip system UI and launcher events
        if (isSystemPackage(packageName)) {
            return
        }

//        Log.d(
//            "MonitorService", "Package: $packageName, Class: $className, currentForegroundApp: $currentForegroundApp"
//        )
        Log.d("MonitorService", "Target Apps: $targetApps")

        // Check if this is a target app
        val isTargetApp = targetApps.contains(packageName)

        // Handle app state changes
        when {
            isTargetApp && currentForegroundApp != packageName -> {
                // Target app opened
                Log.d("MonitorService", "Target app opened: $packageName")
                currentForegroundApp = packageName
                onTargetAppOpened(packageName)
            }

            !isTargetApp && targetApps.contains(currentForegroundApp) -> {
                // Target app closed (another app came to foreground)
                Log.d("MonitorService", "Target app closed: $currentForegroundApp")
                onTargetAppClosed(currentForegroundApp ?: "")
                currentForegroundApp = packageName
            }

            else -> {
                // Non-target app in foreground
                currentForegroundApp = packageName
            }
        }
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return packageName.startsWith("com.android.") ||
                packageName.startsWith("android.") ||
                packageName == "com.google.android.inputmethod.latin" ||
                packageName == "com.android.systemui"
    }

    private fun onTargetAppOpened(packageName: String) {
        showInterceptScreen(packageName)

        // Optional: Send broadcast for other components
        sendBroadcast(Intent("APP_OPENED").apply {
            putExtra("packageName", packageName)
        })
    }

    private fun onTargetAppClosed(packageName: String) {
        Log.d("MonitorService", "Handling app closure: $packageName")

        // Optional: Send broadcast for other components
        sendBroadcast(Intent("APP_CLOSED").apply {
            putExtra("packageName", packageName)
        })

        // You can add specific logic here for when apps are closed
        // For example, show a summary screen or update usage statistics
    }

    private fun showInterceptScreen(packageName: String) {
        val intent = Intent(this, InterceptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("packageName", packageName)
            putExtra("action", "APP_OPENED")
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.d("MonitorService", "Accessibility service interrupted")
        currentForegroundApp = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("MonitorService", "Accessibility service connected")

        // Configure service info for better event filtering
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MonitorService", "Accessibility service destroyed")
        currentForegroundApp = null
    }

    private val targetApps: List<String>
        get() = Preferences.getBlockedApps().filter { it.isNotEmpty() }
}