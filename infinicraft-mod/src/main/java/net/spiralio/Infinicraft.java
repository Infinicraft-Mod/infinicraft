package net.spiralio;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.spiralio.blocks.Infinicrafter;
import net.spiralio.blocks.entity.InfinicrafterEntity;
import net.spiralio.blocks.screen.InfinicrafterScreenHandler;
import net.spiralio.items.Infinite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Infinicraft implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("infinicraft");
	public static final net.spiralio.InfinicraftConfig CONFIG = net.spiralio.InfinicraftConfig.createAndLoad();

	// the "infinite" item that takes any texture
	public static final Item INFINITE = new Infinite(new FabricItemSettings());

	// infinicrafter block
	public static final Block INFINICRAFTER = new Infinicrafter(FabricBlockSettings.create().strength(4.0f));

	// infinicrafter block entity
	public static final BlockEntityType<InfinicrafterEntity> INFINICRAFTER_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("infinicrafter"),
			FabricBlockEntityTypeBuilder.create(InfinicrafterEntity::new, INFINICRAFTER).build()
	);

	// infinicrafter screen handler
	public static final ScreenHandlerType<InfinicrafterScreenHandler> INFINICRAFTER_SCREEN_HANDLER = Registry.register(
			Registries.SCREEN_HANDLER,
			new Identifier("infinicrafter"),
			new ExtendedScreenHandlerType<>(InfinicrafterScreenHandler::new)
	);

	@Override
	public void onInitialize() {
		// Runs as soon as Minecraft loads
		LOGGER.info("Infinicraft is initializing!");

		// Register item, blocks, and block items
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinite"), INFINITE);
		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "infinicrafter"), INFINICRAFTER);
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinicrafter"), new BlockItem(INFINICRAFTER, new FabricItemSettings()));
	}
}