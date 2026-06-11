package com.operationpotato.itemlist.api;

import net.minecraft.client.gui.screens.Screen;

public interface ExcludedScreensManager {
	<T extends Screen> void addProvider(Class<T> screenClass, ExcludedScreenProvider<T> provider);
}
