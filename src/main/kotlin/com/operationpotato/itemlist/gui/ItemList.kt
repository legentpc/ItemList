package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.utils.SkyBlockItems
import com.operationpotato.itemlist.utils.ThreadUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy

class ItemList(x: Int, y: Int, width: Int, height: Int) :
	AbstractContainerWidget(
		x + PADDING, y, width, height,
		Component.empty(), defaultSettings(4)
	) {

	val itemListHeight: Int
		get() = height - 2 * PADDING - 2 * McFont.height

	var layout: PaginatedGridLayout = PaginatedGridLayout(x, y)

	var visibleCols: Int = 0
	var visibleRows: Int = 0
	var horizontalPadding: Int = 0
	var currentPage: Int = 1
	var maxPages: Int = 1

	init {
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
	}

	// Off-Thread
	fun updatePositions() {
		val previouslyVisibleCols = visibleCols
		val previouslyVisibleRows = visibleRows
		val adjustedWidth = width - PADDING
		visibleCols = Math.floorDiv(adjustedWidth, StackDisplay.STACK_WIDTH)
		horizontalPadding = (adjustedWidth - visibleCols * StackDisplay.STACK_WIDTH) / 2
		visibleRows = itemListHeight / StackDisplay.STACK_HEIGHT
		if (visibleCols != previouslyVisibleCols || visibleRows != previouslyVisibleRows) {
			positionDisplays(visibleCols - 1, visibleRows)
		}
		layout.switchPage(currentPage - 1)
	}

	// Off-Thread
	fun positionDisplays(maxCols: Int, maxRows: Int) {
		val newLayout = PaginatedGridLayout(x + horizontalPadding, y + PADDING + McFont.height / 2)
		newLayout.addChildren(children, maxCols, maxRows)
		layout = newLayout
		maxPages = layout.pages
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
			x + width / 2, y + McFont.height, CommonColors.WHITE
		)
		graphics.enableScissor(x, y, x + width, y + height)
		layout.visitPageWidgets {
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

			SkyBlockItems.items.forEach { stack ->
				val display = StackDisplay(stack)
				displays.add(display)
			}

			return displays
		}
	}
}
