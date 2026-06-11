package com.operationpotato.itemlist.api;

import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public interface ExcludedScreenProvider<T extends Screen> {
	/**
	 * Whether the item list should be disabled on the given screen.
	 * Should return the localized mod name, as it is shown to the player if they manually re-open the item list.
	 */
	Optional<String> shouldDisable(T screen);
}
