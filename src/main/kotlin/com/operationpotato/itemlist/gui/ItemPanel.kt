package com.operationpotato.itemlist.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.PageButton
import net.minecraft.network.chat.Component

class ItemPanel(x: Int, y: Int, width: Int, height: Int, val itemListWidget: AbstractItemList) :
	AbstractContainerWidget(x, y, width, height, Component.empty(), defaultSettings(0)) {

	val prevPageButton: Button = PageButton(x + 23, y + 5, false, { _ ->
		itemListWidget.scrollPage(false)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	val nextPageButton: Button = PageButton(x + width - AbstractItemList.PADDING - 15, y + 5, true, { _ ->
		itemListWidget.scrollPage(true)
		playButtonClickSound(Minecraft.getInstance().soundManager)
	}, false)
	val children: List<AbstractWidget> = listOf(nextPageButton, prevPageButton, itemListWidget)

	init {
		itemListWidget.x = x + AbstractItemList.PADDING
		itemListWidget.y = y
		itemListWidget.width = width
		itemListWidget.height = height
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
