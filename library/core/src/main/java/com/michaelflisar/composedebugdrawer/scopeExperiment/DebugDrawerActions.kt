package com.michaelflisar.composedebugdrawer.scopeExperiment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun DebugDrawerLazyListScope.DebugDrawerActions(
    icon: ImageVector? = Icons.Default.Menu,
    label: String = "Debug Drawer Actions",
    collapsible: Boolean = true,
    content: DebugDrawerLazyListScope.() -> Unit = {}
) = LazyDebugDrawerRegion(
    icon = icon,
    label = label,
    collapsible = collapsible,
) {
    lazyDebugDrawerItem {
        val scope = rememberCoroutineScope()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modifier = Modifier.weight(1f)
            DebugDrawerButton(modifier = modifier, label = "Collapse All") {
                collapseAll()
            }
            DebugDrawerButton(modifier = modifier, label = "Close") {
                scope.launch {
                    drawerState.close()
                }
            }
        }
    }

    content()
}
