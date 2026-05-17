package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.utils.ComponentUtils
import com.operationpotato.itemlist.utils.SkyBlockItemCategory
import com.operationpotato.itemlist.utils.ThreadUtils
import com.operationpotato.itemlist.utils.ThreadUtils.cancelAndSubmit
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.PageButton
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.util.CommonColors
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import java.util.concurrent.Future

class ItemPanel(x: Int, y: Int, width: Int, height: Int) :
	AbstractContainerWidget(x, y, width, height, Component.empty(), defaultSettings(0)) {
	val itemListWidget = EntireListWidget(width - AbstractItemList.PADDING, height)

	val prevPageButton: Button = PageButton(0, 0, false, { _ ->
		itemListWidget.scrollPage(false)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	val nextPageButton: Button = PageButton(0, 0, true, { _ ->
		itemListWidget.scrollPage(true)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	var topLayout: LinearLayout = LinearLayout.horizontal()

	val filterButton: CycleButton<SkyBlockItemCategory> =
		CycleButton.builder(SkyBlockItemCategory::asComponent, SkyBlockItemCategory.ALL)
			.withValues(SkyBlockItemCategory.entries)
			.withTooltip(::createFilterTooltip)
			.create(
				x + width - AbstractItemList.PADDING - 7, y + height - 20,
				16, 16, Component.empty(), ::onFilterButtonClick
			)
	val searchBox: EditBox = EditBox(
		McFont.self, x + 20, y + height - 20,
		width - 44, 16,
		Component.empty()
	)

	val children: List<AbstractWidget> = listOf(nextPageButton, prevPageButton, filterButton, searchBox, itemListWidget)

	var filterFuture: Future<*>? = null
	var searchFuture: Future<*>? = null

	init {
		positionTopBar()

		itemListWidget.x = x + AbstractItemList.PADDING
		itemListWidget.y = y
		itemListWidget.positioningCallback = { McClient.runOrNextTick { positionTopBar() } }
		itemListWidget.updatePositionsAsync()

		filterButton.message = Component.literal("F")
		searchBox.setHint(Component.literal("Search..."))
		searchBox.setResponder(::searchAsync)
	}

	fun positionTopBar() {
		val leftPadding = AbstractItemList.PADDING + itemListWidget.horizontalPadding
		val contentWidth = leftPadding + itemListWidget.horizontalPadding + 2 * prevPageButton.width
		topLayout = LinearLayout.horizontal()
		topLayout.setPosition(x + leftPadding, y + 5)
		topLayout.addChild(prevPageButton) { it.alignHorizontallyLeft() }
		topLayout.addChild(SpacerElement.width(width - contentWidth))
		topLayout.addChild(nextPageButton) { it.alignHorizontallyRight() }
		topLayout.arrangeElements()
	}

	fun createFilterTooltip(category: SkyBlockItemCategory): Tooltip {
		val options = ComponentUtils.getCycleEnumOptions(category)
		var line = Component.empty().append(category.asComponent().withStyle(ChatFormatting.GREEN))
		line = line.append(ComponentUtils.joinComponents(options))
		return Tooltip.create(line, null)
	}

	fun onFilterButtonClick(btn: CycleButton<SkyBlockItemCategory>, category: SkyBlockItemCategory) {
		val color = if (category == SkyBlockItemCategory.ALL) {
			ChatFormatting.WHITE
		} else {
			ChatFormatting.GREEN
		}
		btn.message = Component.literal("F").withStyle(color)
		filterAsync(category)
		if (searchBox.value.isNotEmpty()) {
			searchBox.setTextColor(CommonColors.SOFT_YELLOW)
		}
	}

	fun filterAsync(category: SkyBlockItemCategory) {
		ThreadUtils.SORTING_EXECUTOR.cancelAndSubmit(filterFuture) {
			itemListWidget.filterChildren(category)
			itemListWidget.switchPage(0)
			itemListWidget.updatePositionsAsync()
		}
	}

	fun searchAsync(text: String) {
		this.searchFuture = ThreadUtils.SORTING_EXECUTOR.cancelAndSubmit(searchFuture) {
			itemListWidget.searchChildren(text)
			itemListWidget.switchPage(0)
			itemListWidget.updatePositionsAsync()
			if (itemListWidget.visibleChildren.isNotEmpty()) {
				searchBox.setTextColor(CommonColors.TEXT_GRAY)
			} else {
				searchBox.setTextColor(ARGB.opaque(ChatFormatting.RED.color!!))
			}
		}
	}

	override fun children(): List<GuiEventListener> {
		return children
	}

	override fun mouseScrolled(
		x: Double,
		y: Double,
		scrollX: Double,
		scrollY: Double
	): Boolean {
		return itemListWidget.mouseScrolled(x, y, scrollX, scrollY)
	}

	override fun keyPressed(event: KeyEvent): Boolean {
		if (!this.visible) return false
		if (this.searchBox.isFocused) {
			this.searchBox.keyPressed(event)
			return true
		}
		return false
	}

	// if the screen should process this key press.
	fun onScreenKeyPress(screen: Screen, event: KeyEvent): Boolean {
		if (!this.visible) return true
		if (event.isEscape) return true
		if (!this.searchBox.isFocused) return true
		if (screen.focused == this.searchBox) return true
		this.searchBox.keyPressed(event)
		return false
	}

	override fun contentHeight(): Int {
		return height
	}

	override fun extractWidgetRenderState(
		graphics: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		a: Float
	) {
		children.forEach { it.extractRenderState(graphics, mouseX, mouseY, a) }
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}
}
