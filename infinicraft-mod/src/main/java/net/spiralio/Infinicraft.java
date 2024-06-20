package net.spiralio;

import com.google.common.io.CharStreams;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.spiralio.blocks.InfinicrafterBlock;
import net.spiralio.blocks.entity.InfinicrafterBlockEntity;
import net.spiralio.blocks.screen.InfinicrafterScreenHandler;
import net.spiralio.items.InfiniteItem;
import net.spiralio.util.GsonBodyHandler;
import net.spiralio.util.JsonHandler;
import net.spiralio.util.GeneratedItem;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Infinicraft implements ModInitializer {
	private static Infinicraft INSTANCE;

	public static final Logger LOGGER = LoggerFactory.getLogger("infinicraft");
	public static final net.spiralio.InfinicraftConfig CONFIG = net.spiralio.InfinicraftConfig.createAndLoad();

	// the "infinite" item that takes any texture
	public static final Item INFINITE_ITEM = new InfiniteItem(new FabricItemSettings());

	// infinicrafter block
	public static final Block INFINICRAFTER_BLOCK = new InfinicrafterBlock(FabricBlockSettings.create().strength(4.0f));

	// infinicrafter block entity
	public static final BlockEntityType<InfinicrafterBlockEntity> INFINICRAFTER_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier("infinicrafter"),
			FabricBlockEntityTypeBuilder.create(InfinicrafterBlockEntity::new, INFINICRAFTER_BLOCK).build()
	);

	// infinicrafter screen handler
	public static final ScreenHandlerType<InfinicrafterScreenHandler> INFINICRAFTER_SCREEN_HANDLER = Registry.register(
			Registries.SCREEN_HANDLER,
			new Identifier("infinicrafter"),
			new ExtendedScreenHandlerType<>(InfinicrafterScreenHandler::new)
	);

	private static final String DEFAULT_PROMPT = """
		You are an API that takes a combination of items in the form "item + item" and find a suitable single JSON output that combines the two.
		
		REQUIRED PARAMETERS:
		
		item (String): The output word. Items can be physical things, or concepts such as time or justice. Be creative, and don't shy from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)
		
		description (String): A visual description of the item, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM IN THE DESCRIPTION.
		
		throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.
		
		nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable.  Very nutritious items have a value of 1, such as a full steak.
		
		attack (Number): A number between 0 and 1 representing the damage dealt by the item. This can also be interpreted as "hardness". Feathers have 0, rocks have 0.5. Most items should have a value above 0.
		
		color (String): The main color of the item. Can be: black, blue, green, orange, purple, red, yellow.
		
		EXAMPLE INPUT:
		Animal + Water
		
		EXAMPLE OUTPUT:
		{
		"item": "Fish",
		"description": "A large blue fish with black eyes and a big fin.",
		"throwable": true,
		"nutritionalValue": 0.8,
		"attack": 0.2,
		"color": "blue"
		}
		
		MISC EXAMPLES:
		Player Head + Bone = Body
		Show + Sponge = Spongebob
		Sand + Sand = Desert
		""";

	private ExecutorService sdTalkerExecutor;

	private LinkedBlockingQueue<StableDiffusionRequest> requests = new LinkedBlockingQueue<>();

	public record StableDiffusionRequest(String itemDescription, BiConsumer<StableDiffusionResponse, Throwable> callback) {}

	public record StableDiffusionResponse(@SerializedName("success") boolean isSuccess, @SerializedName("image") int @Nullable [] image) {}

	public static CompletableFuture<StableDiffusionResponse> makeStableDiffusionRequest(GeneratedItem itemData) {
		if (INSTANCE == null) {
			throw new Error("Infinicraft was not initialized yet!");
		}

		var task = new CompletableFuture<StableDiffusionResponse>();

        try {
            INSTANCE.requests.put(new StableDiffusionRequest(itemData.getDescription(), (response, ex) -> {
                if (ex != null) {
                    task.completeExceptionally(ex);
                } else {
                    task.complete(response);
                }
            }));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return task;
	}

	@Override
	public void onInitialize() {
		INSTANCE = this;

		// Runs as soon as Minecraft loads
		LOGGER.info("Infinicraft is initializing!");

        try {
			var infinicraftDir = FabricLoader.getInstance().getConfigDir().resolve("infinicraft");
			Files.createDirectories(infinicraftDir);

			var prompt = infinicraftDir.resolve("prompt.txt");
			if (!Files.exists(prompt)) {
				Files.writeString(prompt, DEFAULT_PROMPT);
			}
        } catch (IOException e) {
            LOGGER.error("Failed to create default config for infinicraft", e);
        }

        // Register item, blocks, and block items
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinite"), INFINITE_ITEM);
		Registry.register(Registries.BLOCK, new Identifier("infinicraft", "infinicrafter"), INFINICRAFTER_BLOCK);
		Registry.register(Registries.ITEM, new Identifier("infinicraft", "infinicrafter"), new BlockItem(INFINICRAFTER_BLOCK, new FabricItemSettings()));

		sdTalkerExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
			private static final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r, "Infinicraft SD-API Thread Pool #" + threadNumber.getAndIncrement());
                t.setDaemon(false);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			sdTalkerExecutor.execute(() -> {
				LOGGER.info("Starting infinicraft backend server daemon");

				@SuppressWarnings("resource") var httpClient = HttpClient.newBuilder()
						.executor(sdTalkerExecutor)
						.build();

                try {
					while (!Thread.currentThread().isInterrupted()) {
						var sdRequest = requests.take();
						var httpRequest = HttpRequest.newBuilder()
								.GET()
								.uri(new URIBuilder(new URI(CONFIG.SD_DAEMON_BASE()+"/generate"))
										.setParameter("itemDescription", sdRequest.itemDescription)
										.build())
								.build();

						httpClient.sendAsync(httpRequest, GsonBodyHandler.ofJson(JsonHandler.GSON, StableDiffusionResponse.class))
								.thenAccept(response -> {
									if (response.statusCode() >= 400 || response.statusCode() < 200) {
										var body = response.body().get();

										LOGGER.error("SD failure: Status code {}; body: {}", response.statusCode(), body);
										sdRequest.callback.accept(body, null);
										return;
									}

									var body = response.body().get();
									sdRequest.callback.accept(body, null);
                                })
								.exceptionally(ex -> {
									sdRequest.callback.accept(null, ex);
									return null;
								});
                    }
				} catch (InterruptedException e) {
					// empty
				} catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

				LOGGER.info("Ending infinicraft backend server daemon");
            });
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Shutting down infinicraft backend server daemon");
            try {
                //noinspection ResultOfMethodCallIgnored
                sdTalkerExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }

            sdTalkerExecutor.shutdownNow();
		});
	}
}