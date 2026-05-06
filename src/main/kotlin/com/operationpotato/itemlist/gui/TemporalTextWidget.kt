package com.operationpotato.itemlist.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.util.CommonColors

class TemporalTextWidget(x: Int, y: Int, var time: Float, message: Component, val font: Font) :
	AbstractWidget(x - font.width(message) / 2, y, font.width(message), font.lineHeight, message) {

	override fun extractWidgetRenderState(
		graphics: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		a: Float
	) {
		time -= a
		if (time < 0) return
		graphics.fill(x - PADDING, y - PADDING, x + width + PADDING, y + height + PADDING, BACKGROUND_COLOR)
		graphics.text(font, message, x, y, CommonColors.WHITE)
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}

	fun expired(): Boolean = time < 0

	companion object {
		const val PADDING = 2
		val BACKGROUND_COLOR = ARGB.multiplyAlpha(CommonColors.BLACK, 0.5f)
	}
}
