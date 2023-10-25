package com.michaelflisar.composedebugdrawer.scopeExperiment

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.michaelflisar.composedebugdrawer.core.DebugDrawerDefaults
import com.michaelflisar.composedebugdrawer.core.composables.SegmentedButtons
import com.michaelflisar.composedebugdrawer.core.composables.Spinner
import kotlinx.coroutines.launch

interface DebugDrawerScope {
	val drawerState: DrawerState

	fun toggleExpanded(id: String)
	fun collapseAll()
	fun collapse(vararg ids: String)
	fun expand(vararg ids: String)
	fun isExpanded(id: String, collapsible: Boolean): Boolean
}

@Stable
data class DebugDrawerScopeImpl(
	override val drawerState: DrawerState,
	private val expandSingleOnly: Boolean = false,
	private val expandedIds: MutableState<List<String>>
) : DebugDrawerScope {
	override fun toggleExpanded(id: String) {
		if (expandedIds.value.contains(id)) {
			expandedIds.value = expandedIds.value - id
		} else {
			if (expandSingleOnly)
				expandedIds.value = listOf(id)
			else
				expandedIds.value = expandedIds.value + id
		}
	}

	override fun collapseAll() {
		expandedIds.value = listOf()
	}

	override fun collapse(vararg ids: String) {
		expandedIds.value = expandedIds.value - ids.toSet()
	}

	override fun expand(vararg ids: String) {
		expandedIds.value = ids.toList()
	}

	override fun isExpanded(id: String, collapsible: Boolean): Boolean =
		!collapsible || expandedIds.value.contains(id)
}

@Composable
fun rememberDebugDrawerScope(
	initialValue: DrawerValue = DrawerValue.Closed,
	expandSingleOnly: Boolean = false,
	confirmStateChange: (DrawerValue) -> Boolean = { true },
	initialExpandedIds: List<String> = emptyList()
): DebugDrawerScope {
	val drawerState = rememberSaveable(
		initialValue,
		confirmStateChange,
		saver = DrawerState.Saver(confirmStateChange)
	) {
		DrawerState(initialValue, confirmStateChange)
	}
	val expandedIds = rememberSaveable { mutableStateOf(initialExpandedIds) }
	LaunchedEffect(expandSingleOnly) {
		if (expandSingleOnly && expandedIds.value.size > 1) {
			expandedIds.value = emptyList()
		}
	}
	return DebugDrawerScopeImpl(drawerState, expandSingleOnly, expandedIds)
}

@Composable
fun DebugDrawerWithScope(
	enabled: Boolean = true,
	drawerContent: DebugDrawerLazyListScope.() -> Unit,
	drawerScope: DebugDrawerScope = rememberDebugDrawerScope(),
	modifier: Modifier = Modifier,
	gesturesEnabled: Boolean = true,
	scrimColor: Color = DrawerDefaults.scrimColor,
	drawerOpenMinSpaceLeft: Dp = DebugDrawerDefaults.DEFAULT_MIN_SPACE_LEFT,
	drawerItemSpacing: Dp = DebugDrawerDefaults.ITEM_SPACING,
	drawerContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
	content: @Composable () -> Unit
) {
	val context = LocalContext.current
	if (!enabled) {
		content()
		return
	}

	val coroutineScope = rememberCoroutineScope()
	BackHandler(enabled = drawerScope.drawerState.isOpen) {
		coroutineScope.launch {
			drawerScope.drawerState.close()
		}
	}

	val direction = LocalLayoutDirection.current
	CompositionLocalProvider(
		LocalLayoutDirection provides LayoutDirection.Rtl
	) {
		ModalNavigationDrawer(
			drawerContent = {
				ModalDrawerSheet(
					modifier.padding(end = drawerOpenMinSpaceLeft)
				) {
					CompositionLocalProvider(
						LocalLayoutDirection provides direction
					) {
						LazyColumn(
							modifier = Modifier
								.sizeIn(
									minWidth = 240.dp,//MinimumDrawerWidth,
									maxWidth = DrawerDefaults.MaximumDrawerWidth
								),
							contentPadding = drawerContentPadding,
							verticalArrangement = Arrangement.spacedBy(drawerItemSpacing),
						) {
							drawerContent(
								DebugDrawerLazyListScope(
									drawerScope,
									lazyListScope = this,
									context
								)
							)
						}
					}
				}
			},
			modifier = modifier,
			drawerState = drawerScope.drawerState,
			content = {
				CompositionLocalProvider(
					LocalLayoutDirection provides direction
				) {
					content()
				}
			},
			gesturesEnabled = gesturesEnabled,
			scrimColor = scrimColor
		)
	}
}

class DebugDrawerLazyListScope(
	private val drawerScope: DebugDrawerScope,
	private val lazyListScope: LazyListScope,
	val context: Context
) : LazyListScope by lazyListScope, DebugDrawerScope by drawerScope


// -------------
// Content Items
// -------------

fun DebugDrawerLazyListScope.LazyDebugDrawerRegion(
	icon: ImageVector? = null,
	label: String,
	id: String = label,
	description: String = "",
	collapsible: Boolean = true,
	itemSpacing: Dp = DebugDrawerDefaults.ITEM_SPACING,
	content: DebugDrawerLazyListScope.() -> Unit
) = lazyDebugDrawerItem {
	Column {
		val screenHeight = LocalConfiguration.current.screenHeightDp
		val context = LocalContext.current
		// Transition
		val transitionState = remember {
			MutableTransitionState(this@LazyDebugDrawerRegion.isExpanded(id, collapsible))
		}
		transitionState.targetState = isExpanded(id, collapsible)
		val transition = updateTransition(transitionState, label = "transition")

		val arrowRotationDegree by transition.animateFloat(
			transitionSpec = { tween() },
			label = "arrow",
			targetValueByState = {
				if (it) -180f else 0f
			}
		)

		// Header
		Row(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.then(
					if (collapsible)
						Modifier.clickable { toggleExpanded(id) }
					else Modifier
				)
				.padding(vertical = DebugDrawerDefaults.ITEM_PADDING, horizontal = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			if (icon != null) {
				Icon(
					modifier = Modifier.padding(end = 4.dp),
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary
				)
			}
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = label,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimary
				)
				if (description.isNotEmpty()) {
					Text(
						text = label,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onPrimary
					)
				}
			}

			if (collapsible) {
				Icon(
					modifier = Modifier
						.padding(start = 4.dp)
						.size(24.dp)
						.rotate(arrowRotationDegree),
					imageVector = Icons.Default.KeyboardArrowDown,
					contentDescription = "",
					tint = MaterialTheme.colorScheme.onPrimary
				)
			}
		}

		// Content
		AnimatedVisibilityExpand(
			visible = isExpanded(id, collapsible)
		) {
			LazyColumn(
				// to prevent it from calculating infinite height
				Modifier.heightIn(max = screenHeight.dp),
				contentPadding = PaddingValues(DebugDrawerDefaults.ITEM_PADDING),
				verticalArrangement = Arrangement.spacedBy(itemSpacing)
			) {
				content(
					DebugDrawerLazyListScope(
						this@LazyDebugDrawerRegion,
						lazyListScope = this,
						context
					)
				)
			}
		}
	}
}

fun DebugDrawerLazyListScope.DebugDrawerRegion(
	icon: ImageVector? = null,
	label: String,
	id: String = label,
	description: String = "",
	collapsible: Boolean = true,
	itemSpacing: Dp = DebugDrawerDefaults.ITEM_SPACING,
	content: @Composable ColumnScope.() -> Unit
) = lazyDebugDrawerItem {
	Column {
		val screenHeight = LocalConfiguration.current.screenHeightDp
		val context = LocalContext.current
		// Transition
		val transitionState = remember {
			MutableTransitionState(this@DebugDrawerRegion.isExpanded(id, collapsible))
		}
		transitionState.targetState = isExpanded(id, collapsible)
		val transition = updateTransition(transitionState, label = "transition")

		val arrowRotationDegree by transition.animateFloat(
			transitionSpec = { tween() },
			label = "arrow",
			targetValueByState = {
				if (it) -180f else 0f
			}
		)

		// Header
		Row(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.then(
					if (collapsible)
						Modifier.clickable { toggleExpanded(id) }
					else Modifier
				)
				.padding(vertical = DebugDrawerDefaults.ITEM_PADDING, horizontal = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(4.dp)
		) {
			if (icon != null) {
				Icon(
					modifier = Modifier.padding(end = 4.dp),
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary
				)
			}
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = label,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimary
				)
				if (description.isNotEmpty()) {
					Text(
						text = label,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onPrimary
					)
				}
			}

			if (collapsible) {
				Icon(
					modifier = Modifier
						.padding(start = 4.dp)
						.size(24.dp)
						.rotate(arrowRotationDegree),
					imageVector = Icons.Default.KeyboardArrowDown,
					contentDescription = "",
					tint = MaterialTheme.colorScheme.onPrimary
				)
			}
		}

		// Content
		AnimatedVisibilityExpand(visible = isExpanded(id, collapsible)) {
			Column(
				modifier = Modifier.padding(all = DebugDrawerDefaults.ITEM_PADDING),
				verticalArrangement = Arrangement.spacedBy(itemSpacing)
			) {
				content()
			}
		}
	}
}

fun DebugDrawerLazyListScope.AnimatedDebugDrawerSubRegion(
	visible: Boolean,
	itemSpacing: Dp = DebugDrawerDefaults.ITEM_SPACING,
	content: @Composable ColumnScope.() -> Unit
) = lazyDebugDrawerItem {
	AnimatedVisibilityExpand(visible = visible) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = DebugDrawerDefaults.ITEM_PADDING),
			verticalArrangement = Arrangement.spacedBy(itemSpacing)
		) {
			content()
		}
	}
}



fun DebugDrawerLazyListScope.DebugDrawerButton(
	modifier: Modifier = Modifier.fillMaxWidth(),
	icon: ImageVector? = null,
	foregroundTint: @Composable (() -> Color?) = { null },
	label: String,
	outline: Boolean = true,
	onClick: () -> Unit
) = lazyDebugDrawerItem {
	if (outline) {
		OutlinedButton(
			modifier = modifier,
			onClick = onClick
		) {
			if (icon != null) {
				Icon(
					modifier = Modifier.padding(end = 4.dp),
					imageVector = icon,
					contentDescription = null,
					tint = foregroundTint() ?: LocalContentColor.current
				)
			}
			Text(text = label, color = foregroundTint() ?: Color.Unspecified)
		}
	} else {
		Button(
			modifier = modifier,
			onClick = onClick,
			colors = foregroundTint()?.let {
				ButtonDefaults.buttonColors(contentColor = foregroundTint()!!)
			} ?: ButtonDefaults.buttonColors()
		) {
			if (icon != null) {
				Icon(
					modifier = Modifier.padding(end = 4.dp),
					imageVector = icon,
					contentDescription = null,
					tint = foregroundTint() ?: LocalContentColor.current
				)
			}
			Text(text = label, color = foregroundTint() ?: Color.Unspecified)
		}
	}
}

fun DebugDrawerLazyListScope.DebugDrawerCheckbox(
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	foregroundTint: Color? = null,
	label: String,
	description: String = "",
	checked: Boolean,
	onCheckedChange: (checked: Boolean) -> Unit
) = lazyDebugDrawerItem {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (icon != null) {
			Icon(
				modifier = Modifier.padding(end = 4.dp),
				imageVector = icon,
				contentDescription = null,
				tint = foregroundTint ?: LocalContentColor.current
			)
		}

		TextWithDescription(
			modifier = Modifier.weight(1f),
			label,
			description,
			foregroundTint
		)

		// Checkbox should not be larger than icons to save some space...
		Checkbox(
			modifier = Modifier.height(24.dp),
			checked = checked,
			onCheckedChange = onCheckedChange
		)
	}
}

fun <T> DebugDrawerLazyListScope.DebugDrawerDropdown(
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	label: String,
	selected: T,
	items: List<T>,
	labelProvider: (item: T) -> String = { it.toString() },
	iconProvider: ((item: T) -> ImageVector)? = null,
	onItemSelected: (item: T) -> Unit
) = lazyDebugDrawerItem {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (icon != null) {
			Icon(
				modifier = Modifier.padding(end = 4.dp),
				imageVector = icon,
				contentDescription = null
			)
		}
		val expanded = remember { mutableStateOf(false) }
		Spinner(
			modifier = Modifier.weight(1f),
			expanded = expanded,
			label = label,
			selected = selected,
			items = items,
			labelProvider = labelProvider,
			iconProvider = iconProvider,
			onItemSelected = onItemSelected
		)
	}
}


//TODO: Name properly
@Composable
fun <T> DebugDrawerLazyListScope.DebugDrawerDropdownNormal(
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	label: String,
	selected: T,
	items: List<T>,
	labelProvider: (item: T) -> String = { it.toString() },
	iconProvider: ((item: T) -> ImageVector)? = null,
	onItemSelected: (item: T) -> Unit
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (icon != null) {
			Icon(
				modifier = Modifier.padding(end = 4.dp),
				imageVector = icon,
				contentDescription = null
			)
		}
		val expanded = remember { mutableStateOf(false) }
		Spinner(
			modifier = Modifier.weight(1f),
			expanded = expanded,
			label = label,
			selected = selected,
			items = items,
			labelProvider = labelProvider,
			iconProvider = iconProvider,
			onItemSelected = onItemSelected
		)
	}
}

fun <T> DebugDrawerLazyListScope.DebugDrawerSegmentedButtons(
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	selected: MutableState<T>,
	items: List<T>,
	labelProvider: (item: T) -> String = { it.toString() }
) = DebugDrawerSegmentedButtons(
	modifier,
	icon,
	selected.value,
	items,
	labelProvider
) {
	selected.value = it
}


fun <T> DebugDrawerLazyListScope.DebugDrawerSegmentedButtons(
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	selected: T,
	items: List<T>,
	labelProvider: (item: T) -> String = { it.toString() },
	onItemSelected: (item: T) -> Unit
) = lazyDebugDrawerItem {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (icon != null) {
			Icon(
				modifier = Modifier.padding(end = 4.dp),
				imageVector = icon,
				contentDescription = null
			)
		}
		val index by remember(selected) {
			derivedStateOf { items.indexOf(selected) }
		}
		SegmentedButtons(
			modifier = Modifier.weight(1f),
			items = items.map { labelProvider(it) },
			selectedIndex = index,
			onItemSelected = { onItemSelected(items[it]) }
		)
	}
}


fun DebugDrawerLazyListScope.DebugDrawerInfo(
	modifier: Modifier = Modifier,
	title: String,
	info: String
) = lazyDebugDrawerItem {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			modifier = Modifier.weight(DebugDrawerDefaults.TITLE_TO_TEXT_RATIO),
			text = title,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold
		)
		Text(
			modifier = Modifier.weight(1f - DebugDrawerDefaults.TITLE_TO_TEXT_RATIO),
			text = info,
			style = MaterialTheme.typography.bodySmall
		)
	}
}

fun DebugDrawerLazyListScope.DebugDrawerDivider(
	modifier: Modifier = Modifier.fillMaxWidth(),
	info: String
) = lazyDebugDrawerItem{
	if (info.isEmpty()) {
		Divider(
			modifier = modifier,
			color = MaterialTheme.colorScheme.outline
		)
	} else {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Divider(
				modifier = Modifier.weight(1f),
				color = MaterialTheme.colorScheme.outline
			)
			Text(
				modifier = Modifier
					.weight(1f)
					.padding(horizontal = 8.dp),
				text = info,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center,
				//color = MaterialTheme.colorScheme.primary
			)
			Divider(
				modifier = Modifier.weight(1f),
				color = MaterialTheme.colorScheme.outline
			)
		}
	}
}
//
//// ----------------
//// Helper functions
//// ----------------
//

@Composable
private fun TextWithDescription(
	modifier: Modifier,
	label: String,
	description: String,
	color: Color? = null
) {
	Column(
		modifier = modifier
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold, //if (description.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
			color = color ?: Color.Unspecified
		)
		if (description.isNotEmpty()) {
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = color ?: Color.Unspecified
			)
		}
	}
}

@Composable
private fun AnimatedVisibilityExpand(
	visible: Boolean,
	modifier: Modifier = Modifier,
	content: @Composable AnimatedVisibilityScope.() -> Unit
) {
	val enterTransition = remember {
		expandVertically(
			expandFrom = Alignment.Top
		) + fadeIn(
			initialAlpha = 0.3f
		)
	}
	val exitTransition = remember {
		shrinkVertically(
			// Expand from the top.
			shrinkTowards = Alignment.Top,
			animationSpec = tween()
		) + fadeOut(
			// Fade in with the initial alpha of 0.3f.
			animationSpec = tween()
		)
	}

	AnimatedVisibility(
		visible = visible,
		modifier = modifier,
		enter = enterTransition,
		exit = exitTransition
	) {
		content()
	}
}

fun DebugDrawerLazyListScope.lazyDebugDrawerItem(
	content: @Composable () -> Unit
) = item { content() }