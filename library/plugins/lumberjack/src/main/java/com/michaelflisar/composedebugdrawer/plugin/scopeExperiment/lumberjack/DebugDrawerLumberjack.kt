package com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.lumberjack

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerButton
import com.michaelflisar.composedebugdrawer.scopeExperiment.LazyDebugDrawerRegion
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope
import com.michaelflisar.lumberjack.FileLoggingSetup
import com.michaelflisar.lumberjack.L
import com.michaelflisar.lumberjack.sendFeedback
import com.michaelflisar.lumberjack.showLog

@OptIn(ExperimentalMaterial3Api::class)
fun DebugDrawerLazyListScope.DebugDrawerLumberjack(
    setup: FileLoggingSetup,
    mailReceiver: String,
    icon: ImageVector? = Icons.Default.Description,
    label: String = "Logging",
    id: String = label,
    collapsible: Boolean = true,
    content: DebugDrawerLazyListScope.() -> Unit = {}
) = LazyDebugDrawerRegion(
    icon = icon,
    label = label,
    id = id,
    collapsible = collapsible,
) {
    DebugDrawerButton(
        label = "View Log File",
        icon = Icons.Default.Visibility
    ) {
        L.showLog(context, setup, mailReceiver)
    }
    DebugDrawerButton(
        label = "Send Log File",
        icon = Icons.Default.Email
    ) {
        val file = setup.getLatestLogFiles()
        if (file != null) {
            L.sendFeedback(
                context,
                file,
                mailReceiver,
                filesToAppend = listOf(file)
            )
        } else {
            Toast.makeText(context, "No log file found!", Toast.LENGTH_SHORT).show()
        }
    }
    DebugDrawerButton(
        label = "Clear Log File",
        icon = Icons.Default.Delete,
        foregroundTint = { MaterialTheme.colorScheme.error }
    ) {
        setup.clearLogFiles()
    }
    content()
}