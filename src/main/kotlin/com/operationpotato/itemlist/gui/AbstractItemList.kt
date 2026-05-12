package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.api.impl.PluginManager
import com.operationpotato.itemlist.utils.ThreadUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import kotlin.math.roundToInt

abstract class AbstractItemList(width: Int, height: Int) :
	AbstractContainerWidget(
		0, 0, width, height, Component.empty(), defaultSettings(8)
	) {

	val itemListHeight: Int
		get() = height - 2 * PADDING - 2 * McFont.height

	var layout: PaginatedGridLayout = PaginatedGridLayout(0, 0)
	var itemScale: Float = 1f
	var scrollAmountWidget: TemporalTextWidget? = null

	var visibleCols: Int = 0
	var visibleRows: Int = 0
	var horizontalPadding: Int = 0
	var currentPage: Int = 1
	var maxPages: Int = 1

	init {
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
	}

	abstract fun getItems(): List<StackDisplay>

	// Off-Thread
	fun updatePositions() {
		val previouslyVisibleCols = visibleCols
		val previouslyVisibleRows = visibleRows
		val adjustedWidth = width - PADDING

		val scaledSize = (StackDisplay.STACK_SIZE * itemScale).roundToInt()
		visibleCols = Math.floorDiv(adjustedWidth, scaledSize)
		horizontalPadding = (adjustedWidth - visibleCols * scaledSize) / 2
		visibleRows = itemListHeight / scaledSize
		if ((PluginManager.didExclusionZonesChange() && layout.compareExcludedAreas()) ||
			visibleCols != previouslyVisibleCols || visibleRows != previouslyVisibleRows
		) {
			positionDisplays(visibleCols - 1, visibleRows, scaledSize)
		}
		layout.switchPage(currentPage - 1)
	}

	// Off-Thread
	fun positionDisplays(maxCols: Int, maxRows: Int, scaledSize: Int) {
		val newLayout = PaginatedGridLayout(x + horizontalPadding, y + PADDING + McFont.height / 2)
		getItems().forEach { it.scale(itemScale) }
		newLayout.addChildren(getItems(), maxCols, maxRows, scaledSize)
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

	override fun children(): List<GuiEventListener> = getItems()

	fun scrollPage(scrollDown: Boolean) {
		if (scrollDown) {
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
	}

	fun scrollItemSize(scrollY: Double) {
		itemScale += scrollY.toFloat() / 25
		itemScale = itemScale.coerceIn(1f, 3f)
		scrollAmountWidget = TemporalTextWidget(
			x + width / 2, itemListHeight / 2, 5f,
			Component.literal("${(itemScale * 100).toInt()}%"), McFont.self
		)
		ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
	}

	override fun mouseScrolled(
		x: Double,
		y: Double,
		scrollX: Double,
		scrollY: Double
	): Boolean {
		if (!visible) return false
		if (!this.isMouseOver(x, y)) return false
		if (McClient.self.hasControlDown()) {
			scrollItemSize(scrollY)
		} else {
			scrollPage(scrollY < 0)
		}
		return true
	}

	override fun extractWidgetRenderState(
		graphics: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		a: Float
	) {
		if (PluginManager.didExclusionZonesChange()) {
			ThreadUtils.SORTING_EXECUTOR.execute(::updatePositions)
		}
		graphics.centeredText(
			McFont.self, Component.literal("${currentPage}/${maxPages}"),
			x + width / 2, y + McFont.height, CommonColors.WHITE
		)
		graphics.enableScissor(x, y, x + width, y + height)
		layout.visitPageWidgets {
			it.extractRenderState(graphics, mouseX, mouseY, a)
		}
		scrollAmountWidget?.extractRenderState(graphics, mouseX, mouseY, a)
		if (scrollAmountWidget?.expired() == true) scrollAmountWidget = null
		graphics.disableScissor()
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}

	companion object {
		const val PADDING = StackDisplay.STACK_SIZE
	}
}
