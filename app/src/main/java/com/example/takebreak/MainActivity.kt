package com.example.takebreak

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.takebreak.screens.HomeScreen
import com.example.takebreak.ui.theme.Black
import com.example.takebreak.utils.Preferences

// dribble ui - https://dribbble.com/shots/25743308-Digital-detox-Mobile-app


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if accessibility service is enabled
//        if (!isAccessibilityServiceEnabled(this, AppMonitorAccessibilityService::class.java)) {
//            // Direct user to enable it
//            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
////            Toast.makeText(this, "Enable the accessibility service for monitoring apps", Toast.LENGTH_LONG).show()
//        }

        // Optional: Also check for usage stats permission if you want open count
//        if (!hasUsageStatsPermission(this)) {
//            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
//            Toast.makeText(this, "Grant Usage Access to track app usage count", Toast.LENGTH_LONG).show()
//        }

        setContent {
            val context = LocalContext.current

//            LaunchedEffect(Unit) {
//                Preferences.init(context)
//            }

            MaterialTheme {
                Scaffold { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Black)
                            .padding(innerPadding),
                    ) {
                        HomeScreen()
                    }
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        val expectedService = ComponentName(context, serviceClass).flattenToShortString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").contains(expectedService)
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

//    @SuppressLint("ServiceCast")
//    private fun isAccessibilityServiceEnabledNew(context: Context): Boolean {
//        val manager: AccessibilityManager? =
//            context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager?
//        if (manager == null) return false
//
//        val enabledServices: MutableList<AccessibilityServiceInfo> =
//            manager.getEnabledAccessibilityServiceList(
//                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
//            )
//        for (service in enabledServices) {
//            if (service.getId().contains(context.getPackageName()) &&
//                service.getId().contains(AccessibilityServiceExtend::class.java.getSimpleName())
//            ) {
//                return true
//            }
//        }
//        return false
//    }
}
