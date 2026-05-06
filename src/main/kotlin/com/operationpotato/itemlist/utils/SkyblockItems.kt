package com.operationpotato.itemlist.utils

import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.repo.LazyItemStack
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy

object SkyblockItems {
	val itemNames by registryBoundLazy { getAllItemNames() }
	val items by registryBoundLazy { getAllItems() }

	fun getAllItemNames(): List<String> {
		return RepoAPI.items().items().keys.sorted()
	}

	fun getAllItems(): List<LazyItemStack> {
		if (!RepoAPI.isInitialized()) return listOf()
		val allItems: MutableList<LazyItemStack> = mutableListOf()

		itemNames.forEach { key ->
			val stack = SkyBlockItemsRepo.getLazyItemStack(key) ?: return@forEach
			allItems.add(stack)
		}

		return allItems
	}
}
