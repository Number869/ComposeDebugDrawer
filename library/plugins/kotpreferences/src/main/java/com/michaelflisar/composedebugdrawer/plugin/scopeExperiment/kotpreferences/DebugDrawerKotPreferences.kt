package com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.kotpreferences

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.michaelflisar.composedebugdrawer.plugin.kotpreferences.getDebugLabel
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerCheckbox
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerDropdown
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerDropdownNormal
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerSegmentedButtons
import com.michaelflisar.kotpreferences.core.interfaces.StorageSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun DebugDrawerLazyListScope.DebugDrawerSettingCheckbox(
    modifier: Modifier = Modifier,
    setting: StorageSetting<Boolean>,
    icon: ImageVector? = null,
    foregroundTint: Color? = null,
    label: String = setting.getDebugLabel(),
    description: String = ""
) = DebugDrawerCheckbox(
    modifier = modifier,
    icon = icon,
    foregroundTint = foregroundTint,
    label = label,
    description = description,
    checked = setting.value,
    onCheckedChange = {
        CoroutineScope(Dispatchers.IO).launch {
            setting.update(it)
        }
    }
)

fun <E : Enum<E>> DebugDrawerLazyListScope.DebugDrawerSettingDropdown(
    modifier: Modifier = Modifier,
    setting: StorageSetting<E>,
    items: Array<E>,
    icon: ImageVector? = null,
    label: String = setting.getDebugLabel()
) = DebugDrawerDropdown(
    modifier = modifier,
    icon = icon,
    label = label,
    selected = setting.value,
    items = items.toList(),
    labelProvider = { it.name },
    iconProvider = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        setting.update(it)
    }
}


fun <E : Enum<E>> DebugDrawerLazyListScope.DebugDrawerSettingSegmentedButtons(
    modifier: Modifier = Modifier,
    setting: StorageSetting<E>,
    items: Array<E>,
    icon: ImageVector? = null
) = DebugDrawerSegmentedButtons(
    modifier = modifier,
    icon = icon,
    selected = setting.value,
    items = items.toList(),
    labelProvider = { it.name }
) {
    CoroutineScope(Dispatchers.IO).launch {
        setting.update(it)
    }
}
