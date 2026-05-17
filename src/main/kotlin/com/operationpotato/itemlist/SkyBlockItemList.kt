package com.operationpotato.itemlist

import com.operationpotato.itemlist.api.impl.PluginManager
import com.operationpotato.itemlist.gui.EntireListWidget
import com.operationpotato.itemlist.gui.ItemPanel
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.fabricmc.fabric.api.event.Event
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.resources.Identifier
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.right

object SkyBlockItemList : ClientModInitializer {
	val latePhase = Identifier.fromNamespaceAndPath("skyblock-item-list", "late")

	override fun onInitializeClient() {
		ScreenEvents.AFTER_INIT.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase)
		ScreenEvents.AFTER_INIT.register(latePhase, ::addItemListWidget)
	}

	fun addItemListWidget(mc: Minecraft, screen: Screen, w: Int, h: Int) {
		if (!LocationAPI.isOnSkyBlock) return
		if (screen is AbstractContainerScreen<*>) {
			if (screen is InventoryScreen && mc.player?.hasInfiniteMaterials() ?: false) return
			val width = w - screen.right
			if (width < 80) return
			val itemPanel = ItemPanel(screen.right, 0, width, h)

			Screens.getWidgets(screen).add(itemPanel)
			val mouseScroll = ScreenMouseEvents.allowMouseScroll(screen)
			mouseScroll.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase)
			mouseScroll.register(latePhase) { _, x, y, scrollX, scrollY ->
				itemPanel.mouseScrolled(x, y, scrollX, scrollY)
			}
			val keyPress = ScreenKeyboardEvents.allowKeyPress(screen)
			keyPress.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase)
			keyPress.register(latePhase) { screen, event ->
				itemPanel.onScreenKeyPress(screen, event)
			}
			val beforeExtract = ScreenEvents.beforeExtract(screen)
			beforeExtract.addPhaseOrdering(Event.DEFAULT_PHASE, latePhase)
			beforeExtract.register(latePhase) { _, _, _, _, _ ->
				PluginManager.refreshExclusionZones()
			}
		}
	}
}
