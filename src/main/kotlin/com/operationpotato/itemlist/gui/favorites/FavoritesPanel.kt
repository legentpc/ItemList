package com.operationpotato.itemlist.gui.favorites

import com.operationpotato.itemlist.Settings
import com.operationpotato.itemlist.gui.AbstractItemList
import com.operationpotato.itemlist.gui.recipe.AbstractRecipeWidget
import com.operationpotato.itemlist.gui.recipe.RecipeScreen
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import tech.thatgravyboat.repolib.api.recipes.Recipe
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.right
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

class FavoritesPanel(x: Int, y: Int, width: Int, height: Int) :
	AbstractContainerWidget(x, y, width, height, Component.empty(), defaultSettings(0)) {
	val listWidget = FavoritesListWidget(width - AbstractItemList.PADDING, height)

	var activeRecipe: Recipe<*>? = null
	var recipeWidget: AbstractRecipeWidget? = null

	fun updatePosition() {
		val recipe = recipeWidget
		if (recipe != null) {
			recipe.setPosition((width - recipe.width) / 2, 5)
			if (recipe.width > width) {
				Text.of("Your screen is too small to pin this recipe!").withColor(TextColor.RED).send()
				removeRecipe()
				return
			}
		}
		val listWidgetY = recipeWidget?.bottom ?: y
		listWidget.setPosition(x, listWidgetY)
		listWidget.setSize(width - 2, height - listWidgetY - 20)
		listWidget.itemSize = Settings.favoritesItemSize
		listWidget.scaleChildren()
		listWidget.updatePositionsAsync()
	}

	override fun children(): List<GuiEventListener> = listOfNotNull(listWidget, recipeWidget)

	override fun mouseScrolled(x: Double, y: Double, scrollX: Double, scrollY: Double): Boolean {
		return listWidget.mouseScrolled(x, y, scrollX, scrollY)
	}

	override fun keyPressed(event: KeyEvent): Boolean {
		if (!this.visible) return false
		recipeWidget?.let {
			if (it.isHovered && it.keyPressed(event)) return true
		}
		return listWidget.keyPressed(event)
	}

	fun updateWidth() {
		val screen = McScreen.self
		if (screen !is AbstractContainerScreen<*>) return
		x = 0
		width = screen.width - screen.right
		updatePosition()
	}

	fun onScreenKeyPress(screen: Screen, event: KeyEvent): Boolean {
		if (!this.visible) return true
		if (event.isEscape) return true
		return !keyPressed(event)
	}

	fun removed() {
		Settings.favoritesItemSize = listWidget.itemSize
	}

	fun setRecipe(recipe: Recipe<*>) {
		activeRecipe = recipe
		recipeWidget = if (recipe != recipeWidget?.recipe) RecipeScreen.getWidgetForRecipe(recipe) else null
		updatePosition()
	}

	fun removeRecipe() {
		activeRecipe = null
		recipeWidget = null
		updatePosition()
	}

	override fun contentHeight(): Int = height

	override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
		recipeWidget?.extractRenderState(graphics, mouseX, mouseY, a)
		listWidget.extractRenderState(graphics, mouseX, mouseY, a)
	}

	override fun updateWidgetNarration(output: NarrationElementOutput) {}
}
