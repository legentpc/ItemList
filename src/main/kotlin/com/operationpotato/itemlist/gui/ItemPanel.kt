package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.utils.ComponentUtils
import com.operationpotato.itemlist.utils.SkyBlockItemCategory
import com.operationpotato.itemlist.utils.ThreadUtils
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.PageButton
import net.minecraft.network.chat.Component
import java.util.concurrent.Future

class ItemPanel(x: Int, y: Int, width: Int, height: Int, val itemListWidget: EntireListWidget) :
	AbstractContainerWidget(x, y, width, height, Component.empty(), defaultSettings(0)) {

	val prevPageButton: Button = PageButton(x + 23, y + 5, false, { _ ->
		itemListWidget.scrollPage(false)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	val nextPageButton: Button = PageButton(x + width - AbstractItemList.PADDING - 15, y + 5, true, { _ ->
		itemListWidget.scrollPage(true)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	val filterButton: CycleButton<SkyBlockItemCategory> =
		CycleButton.builder(SkyBlockItemCategory::asComponent, SkyBlockItemCategory.ALL)
			.withValues(SkyBlockItemCategory.entries)
			.withTooltip(::createFilterTooltip)
			.create(
				x + width - AbstractItemList.PADDING - 7, y + height - 20,
				16, 16, Component.empty(), ::onFilterButtonClick
			)
	val children: List<AbstractWidget> = listOf(nextPageButton, prevPageButton, filterButton, itemListWidget)

	var filterFuture: Future<*>? = null

	init {
		itemListWidget.x = x + AbstractItemList.PADDING
		itemListWidget.y = y
		itemListWidget.width = width
		itemListWidget.height = height
		filterButton.message = Component.literal("F")
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
	}

	fun filterAsync(category: SkyBlockItemCategory) {
		val future = filterFuture
		if (future != null && !future.isDone) future.cancel(true)
		filterFuture = ThreadUtils.SORTING_EXECUTOR.submit {
			itemListWidget.filterChildren(category)
			itemListWidget.switchPage(0)
			itemListWidget.updatePositionsAsync()
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
