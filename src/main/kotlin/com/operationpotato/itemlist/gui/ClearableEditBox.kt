package com.operationpotato.itemlist.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

class ClearableEditBox(font: Font, width: Int, height: Int, narration: Component) :
	EditBox(font, width, height, narration) {

	override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
		if (!this.isActive) return false
		if (event.button() == 1) {
			value = ""
			return true
		}
		return super.mouseClicked(event, doubleClick)
	}
}
