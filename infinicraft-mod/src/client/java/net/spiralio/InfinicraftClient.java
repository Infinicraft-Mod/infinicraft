package net.spiralio;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.spiralio.plugins.InfiniteModelLoadingPlugin;
import net.spiralio.screen.InfinicrafterScreen;

import static net.spiralio.Infinicraft.INFINITE;

public class InfinicraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Handles custom model loading
		ModelLoadingPlugin.register(new InfiniteModelLoadingPlugin());

		HandledScreens.register(Infinicraft.INFINICRAFTER_SCREEN_HANDLER, InfinicrafterScreen::new);
	}
}