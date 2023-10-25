package com.michaelflisar.composedebugdrawer.scopeExperiment.buildinfos

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.vector.ImageVector
import com.michaelflisar.composedebugdrawer.core.DebugDrawerDefaults
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerInfo
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope
import com.michaelflisar.composedebugdrawer.scopeExperiment.LazyDebugDrawerRegion

fun DebugDrawerLazyListScope.DebugDrawerBuildInfos(
    icon: ImageVector? = null,
    label: String = "Information",
    id: String = label,
    collapsible: Boolean = true,
    content: DebugDrawerLazyListScope.() -> Unit = {}
)  {
    val packageName = context.packageName

    var info: PackageInfo? = null
    try {
        info = if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
    } catch (e: PackageManager.NameNotFoundException) {
    }

    val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info?.longVersionCode?.toString()
    } else {
        @Suppress("DEPRECATION")
        info?.versionCode?.toString()
    } ?: DebugDrawerDefaults.EMPTY
    val versionName = info?.versionName ?: DebugDrawerDefaults.EMPTY
    val debuggable = if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
        DebugDrawerDefaults.TRUE
    } else DebugDrawerDefaults.FALSE
    val debug = DebugDrawerDefaults.TRUE

    LazyDebugDrawerRegion(
        icon = icon,
        label = label,
        id = id,
        collapsible = collapsible,
    ) {
        DebugDrawerInfo(title = "Version Code", info = version)
        DebugDrawerInfo(title = "Version Name", info = versionName)
        DebugDrawerInfo(title = "Package Name", info = packageName)
        DebugDrawerInfo(title = "Debuggable", info = debuggable)
        DebugDrawerInfo(title = "Debug", info = debug)
        content()
    }
}