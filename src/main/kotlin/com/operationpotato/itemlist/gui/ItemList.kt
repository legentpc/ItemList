package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.utils.SkyblockItems
import com.operationpotato.itemlist.utils.ThreadUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy

class ItemList(x: Int, y: Int, width: Int, height: Int) :
	AbstractContainerWidget(
		x, y + PADDING, width, height,
		Component.empty(), defaultSettings(4)
	) {

	val itemListHeight: Int
		get() = height - 3 * PADDING

	var visibleChildren: List<StackDisplay> = mutableListOf()
	var layout: GridLayout = GridLayout()

	var visibleCols: Int = 0
	var visibleRows: Int = 0
	var currentPage: Int = 1
	var maxPages: Int = 0

	init {
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
	}

	// Off-Thread
	fun updatePositions() {
		val previouslyVisibleCols = visibleCols
		val previouslyVisibleRows = visibleRows
		visibleCols = width / StackDisplay.STACK_WIDTH
		visibleRows = itemListHeight / StackDisplay.STACK_HEIGHT
		if (visibleCols != previouslyVisibleCols || visibleRows != previouslyVisibleRows) {
			positionDisplays(visibleCols, visibleRows)
		}
		layout.setPosition(x, y + (currentPage - 1) * visibleCols * PADDING * -1)
		layout.arrangeElements()
		updateVisibility()
	}

	// Off-thread
	fun updateVisibility() {
		val currentlyVisible: MutableList<StackDisplay> = mutableListOf()
		children.forEach { child ->
			child.visible = child.x in x..x + width - PADDING && child.y in y..y + height
			if (child.visible) {
				currentlyVisible.add(child)
			}
		}
		visibleChildren = currentlyVisible
	}

	// Off-Thread
	fun positionDisplays(maxCols: Int, maxRows: Int) {
		layout = GridLayout()
		var col = 0
		var row = 0
		children.forEach { display ->
			layout.addChild(display, row, col)
			col += 1
			if (col > maxCols) {
				col = 0
				row += 1
			}
		}
		maxPages = Math.ceilDiv(row, maxRows) + 2
		currentPage = currentPage.coerceIn(1, maxPages)
	}

	override fun setPosition(x: Int, y: Int) {
		super.setPosition(x, y)
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
	}

	override fun contentHeight(): Int {
		return width
	}

	override fun children(): List<GuiEventListener> = children

	override fun mouseScrolled(
		x: Double,
		y: Double,
		scrollX: Double,
		scrollY: Double
	): Boolean {
		if (!visible) return false
		if (scrollY < 0) {
			currentPage += 1
			if (currentPage > maxPages) {
				currentPage -= maxPages
			}
		} else {
			currentPage -= 1
			if (currentPage < 1) {
				currentPage += maxPages
			}
		}
		currentPage = currentPage.coerceIn(1, maxPages)
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
		return true
	}

	override fun extractWidgetRenderState(
		graphics: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		a: Float
	) {
		graphics.centeredText(
			McFont.self, Component.literal("${currentPage}/${maxPages}"),
			x + width / 2, McFont.height, CommonColors.WHITE
		)
		graphics.enableScissor(x, y, x + width, y + height)
		visibleChildren.forEach {
			it.extractRenderState(graphics, mouseX, mouseY, a)
		}
		graphics.disableScissor()
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}

	companion object {
		private const val PADDING = StackDisplay.STACK_HEIGHT
		private val children: List<StackDisplay> by registryBoundLazy { getItems() }

		fun getItems(): List<StackDisplay> {
			val displays: MutableList<StackDisplay> = mutableListOf()

			SkyblockItems.items.forEach { stack ->
				val display = StackDisplay(stack)
				displays.add(display)
			}

			return displays
		}
	}
}
