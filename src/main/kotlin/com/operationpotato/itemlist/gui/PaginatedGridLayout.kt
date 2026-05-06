package com.operationpotato.itemlist.gui

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer

class PaginatedGridLayout(private var x: Int, private var y: Int) : Layout {
	var activePage = 0
	var pages = 0

	private val gridLayouts = mutableListOf<MarkedGridLayout>()

	fun addChildren(children: List<LayoutElement>, maxCols: Int, maxRows: Int) {
		var page = 0
		var layout = MarkedGridLayout(x, y)
		var col = 0
		var row = 0
		children.forEach { display ->
			layout.addChild(display, row, col)
			col += 1
			if (col > maxCols) {
				col = 0
				row += 1
			}
			if (row > maxRows) {
				row = 0
				col = 0
				gridLayouts.add(layout)
				layout = MarkedGridLayout(x, y)
				page += 1
			}
		}
		gridLayouts.add(layout)
		activePage = 0
		pages = gridLayouts.size
	}

	fun switchPage(page: Int) {
		activePage = page.coerceIn(0, gridLayouts.size)
		arrangeElements()
	}

	fun visitPageWidgets(consumer: Consumer<AbstractWidget>) {
		getPageLayout(activePage)?.visitWidgets(consumer)
	}

	override fun arrangeElements() {
		val layout = getPageLayout(activePage) ?: return
		if (layout.hasBeenArranged) return
		layout.arrangeElements()
	}

	fun getPageLayout(page: Int): MarkedGridLayout? {
		if (gridLayouts.isEmpty() || page >= gridLayouts.size || page < 0) return null
		return gridLayouts[page]
	}

	override fun visitChildren(layoutElementVisitor: Consumer<LayoutElement>) {}

	override fun setX(x: Int) {
		this.x = x
		gridLayouts.forEach { gridLayout ->
			gridLayout.x = x
			gridLayout.hasBeenArranged = false
		}
	}

	override fun setY(y: Int) {
		this.y = y
		gridLayouts.forEach { gridLayout ->
			gridLayout.y = y
			gridLayout.hasBeenArranged = false
		}
	}

	override fun getX(): Int {
		return x
	}

	override fun getY(): Int {
		return y
	}

	override fun getWidth(): Int {
		return getPageLayout(activePage)?.width ?: -1
	}

	override fun getHeight(): Int {
		return getPageLayout(activePage)?.height ?: -1
	}

	class MarkedGridLayout(x: Int, y: Int) : GridLayout(x, y) {
		var hasBeenArranged: Boolean = false

		override fun arrangeElements() {
			super.arrangeElements()
			hasBeenArranged = true
		}

		override fun <T : LayoutElement> addChild(
			child: T,
			row: Int,
			column: Int,
			rows: Int,
			columns: Int,
			cellSettings: LayoutSettings
		): T {
			hasBeenArranged = false
			return super.addChild(child, row, column, rows, columns, cellSettings)
		}
	}
}
