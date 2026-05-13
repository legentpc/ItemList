package com.operationpotato.itemlist.api.impl

import com.google.common.collect.Ordering
import com.operationpotato.itemlist.api.ExclusionZone
import com.operationpotato.itemlist.api.ExclusionZoneManager
import com.operationpotato.itemlist.api.Plugin
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.client.gui.screens.inventory.EffectsInInventory
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.effect.MobEffectInstance
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.extentions.top

class DefaultPlugin : Plugin {
	override fun registerExclusionZones(exclusionZoneManager: ExclusionZoneManager) {
		val screen = McScreen.self ?: return
		val player = McPlayer.self ?: return
		if (!screen.showsActiveEffects() || screen !is InventoryScreen && screen !is CreativeModeInventoryScreen) return
		if (player.activeEffects.isEmpty()) return
		val effectsInInventory = getEffectsInInventory(screen) ?: return

		val playerEffects = Ordering.natural<MobEffectInstance>().sortedCopy(player.activeEffects)

		val x = screen.right + 2
		val availableWidth = screen.width - (x)
		val maxWidth = if (availableWidth >= 120) 120 else 32
		var y = screen.top
		val step = if (player.activeEffects.size > 5) 132 / (player.activeEffects.size) else 33

		playerEffects.forEach { effect ->
			val width = if (maxWidth == 32) maxWidth else getEffectSize(effectsInInventory, effect)
			exclusionZoneManager.addExclusionZone(ExclusionZone.create(x, y, width, 32))
			y += step
		}
	}

	fun getEffectSize(effectsInInventory: EffectsInInventory, effect: MobEffectInstance): Int {
		return 32 + McFont.self.width(effectsInInventory.getEffectName(effect))
	}

	fun getEffectsInInventory(screen: Screen): EffectsInInventory? {
		if (screen is CreativeModeInventoryScreen) {
			return screen.effects
		} else if (screen is InventoryScreen) {
			return screen.effects
		}
		return null
	}
}
