package com.michaelflisar.composedebugdrawer.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelflisar.composedebugdrawer.scopeExperiment.buildinfos.DebugDrawerBuildInfos
import com.michaelflisar.composedebugdrawer.demo.classes.DemoLogging
import com.michaelflisar.composedebugdrawer.demo.classes.DemoPrefs
import com.michaelflisar.composedebugdrawer.scopeExperiment.deviceinfos.DebugDrawerDeviceInfos
import com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.kotpreferences.DebugDrawerSettingCheckbox
import com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.kotpreferences.DebugDrawerSettingDropdown
import com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.kotpreferences.DebugDrawerSettingSegmentedButtons
import com.michaelflisar.composedebugdrawer.plugin.scopeExperiment.lumberjack.DebugDrawerLumberjack
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerActions
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerWithScope
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerButton
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerCheckbox
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerDivider
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerDropdownNormal
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerInfo
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerLazyListScope
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerRegion
import com.michaelflisar.composedebugdrawer.scopeExperiment.LazyDebugDrawerRegion
import com.michaelflisar.composedebugdrawer.scopeExperiment.DebugDrawerSegmentedButtons
import com.michaelflisar.composedebugdrawer.scopeExperiment.rememberDebugDrawerScope
import com.michaelflisar.kotpreferences.compose.collectAsStateNotNull
import com.michaelflisar.lumberjack.L
import com.michaelflisar.testcompose.ui.theme.ComposeDialogDemoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DemoActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // just for demo purposes of the lumberjack module we log all demo pref changes
        // and write at least an initial log...
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                DemoPrefs.changes.collect {
                    L.d { "Preference \"${it.setting.key}\" changed to \"${it.value}\"" }
                }
            }
        }
        L.d { "Demo started" }

        setContent {

            // collectAsState comes from preference module and allows us to simply use MaterialPreferences with compose
            val theme by DemoPrefs.theme.collectAsStateNotNull()
            val dynamicTheme by DemoPrefs.dynamicTheme.collectAsStateNotNull()
            val expandSingleOnly = DemoPrefs.expandSingleOnly.collectAsStateNotNull()

            val scope = rememberCoroutineScope()

            ComposeDialogDemoTheme(
                darkTheme = theme.isDark(),
                dynamicColor = dynamicTheme
            ) {
                // only necessary to access the drawer from Content()

                val debugDrawerScopeState = rememberDebugDrawerScope()
                DebugDrawerWithScope(
                    enabled = true, // if disabled the drawer will not be created at all, in this case inside a release build... could be a (hidden) setting inside your normal settings or whereever you want...
                    drawerContent = {
                        Drawer()
                    },
                    content = {
                        Content(debugDrawerScopeState.drawerState)
                    }
                )
            }
        }
    }

    // ----------------
    // UI - Content and Drawer
    // ----------------

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun Content(drawerState: DrawerState) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val scope = rememberCoroutineScope()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Swipe from right side to open debug drawer",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Text("Open Debug Drawer")
                        }
                    }

                }
            }
        }
    }

    private fun DebugDrawerLazyListScope.Drawer() {
        DebugDrawerCheckbox(
            label = "Expand Single Only",
            description = "This flag is used by this debug drawer!",
            checked = DemoPrefs.expandSingleOnly.value
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                DemoPrefs.expandSingleOnly.update(it)
            }
        }

        // 1) Build Infos
        DebugDrawerBuildInfos(collapsible = true) {
//             optional additional debug drawer entries...
            DebugDrawerInfo(title = "Author", info = "MF")
        }

        // 2) Custom Module
        DebugDrawerAppTheme()

        // 3) Debug Drawer Actions
        DebugDrawerActions()

        // 4) Device Infos
        DebugDrawerDeviceInfos()

        // 5) Lumberjack plugin
        DebugDrawerLumberjack(
            setup = DemoLogging.fileLoggingSetup,
            mailReceiver = "feedback@gmail.com"
        )

        // 6) Example of use with MaterialPreferences and automatic label deduction from properties
        // currently enum based lists + boolean basec checkboxes are supported
        LazyDebugDrawerRegion(
            icon = Icons.Default.ColorLens,
            label = "Demo Preferences"
        ) {
            DebugDrawerDivider(info = "Boolean")
            DebugDrawerSettingCheckbox(setting = DemoPrefs.devBoolean1)
            DebugDrawerSettingCheckbox(setting = DemoPrefs.devBoolean2)

            DebugDrawerDivider(info = "Enum")
            DebugDrawerSettingDropdown(
                setting = DemoPrefs.devStyle,
                items = DemoPrefs.UIStyle.values()
            )
            DebugDrawerSettingSegmentedButtons(
                setting = DemoPrefs.devStyle,
                items = DemoPrefs.UIStyle.values()
            )
        }

        // 7) Example of manual checkboxes, buttons, segmentedbuttons, info texts
        LazyDebugDrawerRegion(
            icon = Icons.Default.Info,
            label = "Manual",
            description = "With some description...",
        ) {
            var test1 by mutableStateOf(false)
            DebugDrawerCheckbox(
                label = "Checkbox",
                description = "Some debug flag",
                checked = test1
            ) {
                test1 = it
            }
            DebugDrawerButton(icon = Icons.Default.BugReport, label = "Button (Filled)") {
                Toast.makeText(this@DemoActivity, "Filled Button Clicked", Toast.LENGTH_SHORT)
                    .show()
            }
            DebugDrawerButton(
                icon = Icons.Default.BugReport,
                outline = false,
                label = "Button (Outlined)"
            ) {
                Toast.makeText(this@DemoActivity, "Outlined Button Clicked", Toast.LENGTH_SHORT)
                    .show()
            }
            DebugDrawerInfo(title = "Custom Info", info = "Value of custom info...")

            val level = mutableStateOf("L1")
            DebugDrawerSegmentedButtons(selected = level, items = listOf("L1", "L2", "L3"))
        }

        // 8) Example of custom layouts
        DebugDrawerRegion(
            icon = Icons.Outlined.Info,
            label = "Custom Layouts",
        ) {

//             2 Buttons in 1 Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modifier = Modifier.weight(1f)
                DebugDrawerButton(
                    modifier = modifier,
                    icon = Icons.Default.Info,
                    label = "B1"
                ) {
                    Toast.makeText(this@DemoActivity, "Button B1 Clicked", Toast.LENGTH_SHORT).show()
                }
                DebugDrawerButton(
                    modifier = modifier,
                    icon = Icons.Default.Info,
                    outline = false,
                    label = "B2"
                ) {
                    Toast.makeText(this@DemoActivity, "Button B2 Clicked", Toast.LENGTH_SHORT).show()
                }
            }

//             2 Dropdowns in 1 row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modifier = Modifier.weight(1f)
                val items = listOf("Entry 1", "Entry 2", "Entry 3")
                var test1 by remember { mutableStateOf(items[0]) }
                var test2 by remember { mutableStateOf(items[1]) }
                DebugDrawerDropdownNormal(
                    modifier = modifier,
                    label = "Test1",
                    selected = test1,
                    items = items
                ) {
                    test1 = it
                }
                DebugDrawerDropdownNormal(
                    modifier = modifier,
                    label = "Test2",
                    selected = test2,
                    items = items
                ) {
                    test2 = it
                }
            }
        }
    }
}