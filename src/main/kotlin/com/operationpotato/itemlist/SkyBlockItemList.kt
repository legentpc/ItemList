package com.operationpotato.itemlist

import com.operationpotato.itemlist.gui.ItemList
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import org.slf4j.LoggerFactory
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.right

object SkyBlockItemList : ClientModInitializer {
	private val logger = LoggerFactory.getLogger("skyblock-item-list")

	override fun onInitializeClient() {
		logger.info("Hello Fabric world!")
		ScreenEvents.AFTER_INIT.register { mc, screen, w, h ->
			if (!LocationAPI.isOnSkyBlock) return@register
			if (screen is AbstractContainerScreen<*>) {
				if (screen is InventoryScreen && mc.player?.hasInfiniteMaterials() ?: false) return@register
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
