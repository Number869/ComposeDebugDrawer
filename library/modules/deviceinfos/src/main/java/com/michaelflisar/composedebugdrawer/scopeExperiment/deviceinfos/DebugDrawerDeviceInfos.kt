package com.michaelflisar.composedebugdrawer.scopeExperiment.deviceinfos

import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerInfo
import com.michaelflisar.composedebugdrawer.scopeExperiment.LazyDebugDrawerRegion
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope

fun DebugDrawerLazyListScope.DebugDrawerDeviceInfos(
    icon: ImageVector? = Icons.Default.PhoneAndroid,
    label: String = "Device",
    id: String = label,
    collapsible: Boolean = true,
    content: DebugDrawerLazyListScope.() -> Unit = {}
) {
    val displayMetrics = context.resources.displayMetrics

    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val resolution = "${displayMetrics.heightPixels} x ${displayMetrics.widthPixels}"
    val density = "${displayMetrics.densityDpi}dpi (${getDensityString(displayMetrics)})"
    val sdk = Build.VERSION.SDK_INT.toString()

    LazyDebugDrawerRegion(
        icon = icon,
        label = label,
        id = id,
        collapsible = collapsible,
    ) {
        DebugDrawerInfo(title = "Manufacturer", info = manufacturer)
        DebugDrawerInfo(title = "Model", info = model)
        DebugDrawerInfo(title = "Resolution", info = resolution)
        DebugDrawerInfo(title = "Density", info = density)
        DebugDrawerInfo(title = "SDK", info = sdk)
        content()
    }
}

private fun getDensityString(displayMetrics: DisplayMetrics): String {
    return when (displayMetrics.densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "LDPI"
        DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
        DisplayMetrics.DENSITY_HIGH -> "HDPI"
        DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
        DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
        DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
        else -> displayMetrics.densityDpi.toString()
    }
}
