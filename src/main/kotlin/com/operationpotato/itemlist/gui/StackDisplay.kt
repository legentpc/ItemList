package com.operationpotato.itemlist.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.api.repo.LazyItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.helpers.McPlayer

class StackDisplay(val lazyStack: LazyItemStack) :
	AbstractWidget(0, 0, STACK_WIDTH, STACK_HEIGHT, Component.empty()) {

	var stack: ItemStack = ItemStack.EMPTY

	fun getTooltipLines(): List<Component> {
		val tooltipStyle = if (McClient.options.advancedItemTooltips) {
			TooltipFlag.Default.ADVANCED
		} else {
			TooltipFlag.Default.NORMAL
		}
		return stack.getTooltipLines(Item.TooltipContext.of(McLevel.self), McPlayer.self, tooltipStyle)
	}

	override fun extractWidgetRenderState(
		graphics: GuiGraphicsExtractor,
		mouseX: Int,
		mouseY: Int,
		a: Float
	) {
		if (stack.isEmpty) stack = lazyStack.create()
		graphics.item(stack, x, y)

		if (isHovered) {
			graphics.setComponentTooltipForNextFrame(McClient.gui.font, getTooltipLines(), x, y)
		}
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}

	companion object {
		const val STACK_WIDTH = 18
		const val STACK_HEIGHT = 18
	}
}
