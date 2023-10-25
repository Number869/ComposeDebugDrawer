package com.michaelflisar.composedebugdrawer.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.michaelflisar.composedebugdrawer.demo.classes.DemoPrefs
import com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.kotpreferences.DebugDrawerSettingCheckbox
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerInfo
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope
import com.michaelflisar.composedebugdrawer.scopeExperiment.LazyDebugDrawerRegion

fun DebugDrawerLazyListScope.DebugDrawerAppTheme(
    icon: ImageVector? = Icons.Default.Style,
    label: String = "App Theme",
    collapsible: Boolean = true
) = LazyDebugDrawerRegion(
    icon = icon,
    label = label,
    collapsible = collapsible,
) {
//    DebugDrawerSettingCheckbox(
//        setting = DemoPrefs.theme,
//        items = DemoTheme.values(),
//        label = "Theme" // optional manual label...
//    )
    DebugDrawerSettingCheckbox(
        setting = DemoPrefs.dynamicTheme,
        label = "Dynamic Colors" // optional manual label...
    )
    DebugDrawerInfo(
        title = "Persistance",
        info = "This demo does persist the theme inside a preferences file - easily achieved with the help of MaterialPreferences storage."
    )
}

