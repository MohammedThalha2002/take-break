package com.example.takebreak.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.takebreak.ui.theme.Black
import com.example.takebreak.ui.theme.Teal700
import com.example.takebreak.ui.theme.White
import com.example.takebreak.utils.Preferences
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    val packages = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    val appList = remember { mutableStateOf(listOf<AppInfo>()) }

    val blockedApps = remember {
        derivedStateOf {
            appList.value.filter { it.isBlocked }
        }
    }

    val unBlockedApps = remember {
        derivedStateOf {
            appList.value.filter { it.isBlocked.not() }
        }
    }

    LaunchedEffect(Unit) {
        Preferences.init(context)
        val blockedApps = Preferences.getBlockedApps()
        appList.value = packages
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map {
                AppInfo(
                    name = it.loadLabel(context.packageManager).toString(),
                    packageName = it.packageName,
                    icon = it.loadIcon(context.packageManager),
                    isBlocked = blockedApps.contains(it.packageName)
                )
            }.sortedBy { it.name.lowercase() }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Selected Apps",
                color = White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(
            count = blockedApps.value.size,
            key = { index -> index }
        ) { index ->
            AppItem(appInfo = blockedApps.value[index])
        }
        item {
            Text(
                text = "Select Apps to Block",
                color = White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(
            count = unBlockedApps.value.size,
            key = { index -> unBlockedApps.value[index].packageName }
        ) { index ->
            AppItem(appInfo = unBlockedApps.value[index])
        }
    }
}

@Composable
fun AppItem(appInfo: AppInfo) {
    val scope = rememberCoroutineScope()
    var isChecked by remember { mutableStateOf(appInfo.isBlocked) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = !isChecked
                appInfo.isBlocked = isChecked
                scope.launch {
                    if (isChecked) {
                        Preferences.addApp(appInfo.packageName)
                    } else {
                        Preferences.removeApp(appInfo.packageName)
                    }
                }
            },
            colors = CheckboxDefaults.colors().copy(
                checkedBoxColor = Teal700,
                checkedBorderColor = Teal700
            ),
            modifier = Modifier.padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
        )
        appInfo.icon?.let {
            val bitmap = appInfo.icon.toBitmap()
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp)
            )
        }
        Text(
            text = appInfo.name,
            modifier = Modifier.padding(8.dp),
            fontSize = 13.sp,
            color = White
        )
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    var isBlocked: Boolean
)