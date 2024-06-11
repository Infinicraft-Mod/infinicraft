package net.spiralio.plugins;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.spiralio.models.InfiniteModel;

public class InfiniteModelLoadingPlugin implements ModelLoadingPlugin {
    public static final ModelIdentifier INFINITE_MODEL = new ModelIdentifier("infinicraft", "block/infinite", "");

    @Override
    public void onInitializeModelLoader(Context pluginContext) {

        pluginContext.modifyModelOnLoad().register((original, context) -> {
            // This calls for every model in the game
            if (context.id().equals(INFINITE_MODEL)) {
                return new InfiniteModel();
            } else {
                return original;
            }

        });
    }
}
