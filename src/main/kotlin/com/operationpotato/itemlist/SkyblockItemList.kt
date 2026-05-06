package com.operationpotato.itemlist

import com.operationpotato.itemlist.gui.ItemList
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.utils.extentions.right

object SkyblockItemList : ClientModInitializer {
	private val logger = LoggerFactory.getLogger("skyblock-item-list")

	override fun onInitializeClient() {
		logger.info("Hello Fabric world!")
		ScreenEvents.AFTER_INIT.register { _, screen, w, h ->
			if (screen is AbstractContainerScreen<*>) {
				val width = w - screen.right
				val itemList = ItemList(screen.right, 0, width, h)

				Screens.getWidgets(screen).add(itemList)
				ScreenMouseEvents.allowMouseScroll(screen).register { _, x, y, scrollX, scrollY ->
					itemList.mouseScrolled(x, y, scrollX, scrollY)
				}
				ScreenKeyboardEvents.allowKeyPress(screen).register { _, event ->
					itemList.keyPressed(event)
					return@register true
				}
			}
		}
	}
}
