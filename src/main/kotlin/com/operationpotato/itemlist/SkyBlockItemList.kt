package com.operationpotato.itemlist

import com.operationpotato.itemlist.gui.EntireListWidget
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.right

object SkyBlockItemList : ClientModInitializer {

	override fun onInitializeClient() {
		ScreenEvents.AFTER_INIT.register(::addItemListWidget)
	}

	fun addItemListWidget(mc: Minecraft, screen: Screen, w: Int, h: Int) {
		if (!LocationAPI.isOnSkyBlock) return
		if (screen is AbstractContainerScreen<*>) {
			if (screen is InventoryScreen && mc.player?.hasInfiniteMaterials() ?: false) return
			val width = w - screen.right
			val itemList = EntireListWidget(screen.right, 0, width, h)

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
