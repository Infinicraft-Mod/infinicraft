package net.spiralio;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

	// building blocks (infinitum)
	public static final Block INFINITUM_BLOCK = new Block(FabricBlockSettings.create().strength(4.0f));
	public static final Block SMOOTH_INFINITUM_BLOCK = new Block(FabricBlockSettings.create().strength(4.0f));
	public static final Block INFINITUM_COLUMN = new Block(FabricBlockSettings.create().strength(4.0f));

	// materials
	public static final Item INFINITUM = new Item(new FabricItemSettings());


	@Override
	public void onInitialize() {
		// Runs as soon as Minecraft loads
		LOGGER.info("Infinicraft is initializing!");

		// Register item, blocks, and block items
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinite"), INFINITE);
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinitum"), INFINITUM);
		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "infinicrafter"), INFINICRAFTER);
		Item infinicrafter_blockitem = Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinicrafter"), new BlockItem(INFINICRAFTER, new FabricItemSettings()));

		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "infinitum_block"), INFINITUM_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "smooth_infinitum_block"), SMOOTH_INFINITUM_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "infinitum_column"), INFINITUM_COLUMN);

		Item infinitum_blockitem = Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinitum_block"), new BlockItem(INFINITUM_BLOCK, new FabricItemSettings()));
		Item smooth_infinitum_blockitem = Registry.register(Registries.ITEM, new Identifier("infinicraft", "smooth_infinitum_block"), new BlockItem(SMOOTH_INFINITUM_BLOCK, new FabricItemSettings()));
		Item infinitum_column_blockitem = Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinitum_column"), new BlockItem(INFINITUM_COLUMN, new FabricItemSettings()));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(infinicrafter_blockitem);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(INFINITUM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
			content.add(infinitum_blockitem);
			content.add(smooth_infinitum_blockitem);
			content.add(infinitum_column_blockitem);
		});

		String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
		String recipesJSONPath = configDir + "/infinicraft/recipes.json";
		String itemsJSONPath = configDir + "/infinicraft/items.json";
		ensureDirectoryAndFilesExist(configDir + "/infinicraft", itemsJSONPath, recipesJSONPath);
	}
	private void ensureDirectoryAndFilesExist(String dirPath, String... filePaths) {
		File dir = new File(dirPath);

		// Ensure the directory exists
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// Ensure each file exists and create it with an empty JSON array if it does not
		for (String filePath : filePaths) {
			File file = new File(filePath);
			if (!file.exists()) {
				try (FileWriter writer = new FileWriter(file)) {
					writer.write("[]");
				} catch (IOException e) {
					throw new RuntimeException("Failed to create " + filePath, e);
				}
			}
		}
	}

}