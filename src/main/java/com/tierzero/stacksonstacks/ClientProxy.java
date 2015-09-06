package com.tierzero.stacksonstacks;

import com.tierzero.stacksonstacks.api.IngotFinder;
import com.tierzero.stacksonstacks.compat.CompatHandler;
import com.tierzero.stacksonstacks.render.RenderTileIngotPile;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	public void registerRenders() {
		RenderingRegistry.registerBlockHandler(SoS.ingotPile.getRenderType(), new RenderTileIngotPile());
	}

	@Override
	public void postInit() {
		IngotFinder.registerIngotColors();
		CompatHandler.clientSide();
	}
}
