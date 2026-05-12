package com.operationpotato.itemlist.gui

import com.operationpotato.itemlist.utils.SkyBlockItems
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy

class EntireListWidget(width: Int, height: Int) : AbstractItemList(width, height) {
	override fun getItems(): List<StackDisplay> {
		return children
	}

	companion object {
		private val children: List<StackDisplay> by registryBoundLazy { getItems() }

		fun getItems(): List<StackDisplay> {
			val displays: MutableList<StackDisplay> = mutableListOf()

			SkyBlockItems.items.forEach { (stack, category) ->
				val display = StackDisplay(stack, category)
				displays.add(display)
			}

			return displays
		}
	}
}
