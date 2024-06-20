package net.spiralio.blocks.entity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.spiralio.Infinicraft;
import net.spiralio.inventory.ImplementedInventory;
import net.spiralio.blocks.screen.InfinicrafterScreenHandler;
import net.spiralio.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class InfinicrafterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    private static final int INPUT_ONE_SLOT = 0;
    private static final int INPUT_TWO_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private static final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r, "Infinicraft Inference Thread Pool #" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    });

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .build();

    protected final PropertyDelegate propertyDelegate;
    private boolean crafting = false;

    public InfinicrafterBlockEntity(BlockPos pos, BlockState state) {
        super (Infinicraft.INFINICRAFTER_ENTITY, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return (crafting) ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
                crafting = (value == 1);
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Infinicrafter");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new InfinicrafterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        crafting = nbt.getBoolean("infinicrafter.crafting");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putBoolean("infinicrafter.crafting", crafting);
    }

    String[] lastRequest = null;

    public void tick(World world, BlockPos pos, BlockState state) throws IOException {
        if (world.isClient()) return;

        ItemStack output = this.getStack(OUTPUT_SLOT);
        ItemStack inputOne = this.getStack(INPUT_ONE_SLOT);
        ItemStack inputTwo = this.getStack(INPUT_TWO_SLOT);

        if (!inputOne.isEmpty() && !inputTwo.isEmpty()) {
            // Time to craft!

            this.crafting = true;

            // Store inputs in a recipe array
            String[] requestedRecipe = {
                    inputOne.getName().getString(),
                    inputTwo.getName().getString(),
            };

            int craftedAmount = Math.min(inputOne.getCount(), inputTwo.getCount());

            // Check if a matching recipe exists
            @Nullable GeneratedRecipe matchingOutput = JsonHandler.getRecipe(requestedRecipe);

            if (matchingOutput != null) {
                // Recipe found
                boolean isOutputSet = false;
                if (!output.isEmpty()) {
                    // don't generate place generatedItem in block generatedItem slot if generatedItem slot is not empty
                    // and the new generatedItem doesn't match the item already in that slot
                    if (!output.getName().getString().equalsIgnoreCase(matchingOutput.getResult())) {
                        return;
                    } else {
                        int totalDiff = output.getCount() + craftedAmount - output.getMaxCount();
                        craftedAmount -= Math.max(totalDiff, 0);
                        this.setStack(OUTPUT_SLOT, output.copyWithCount(output.getCount() + craftedAmount));
                        isOutputSet = true;
                    }
                }

                if (!isOutputSet) {
                    // Check if a Minecraft block exists of matching name
                    for (Block block : Registries.BLOCK) {
                        String blockName = block.getName().getString();

                        if (blockName.equalsIgnoreCase(matchingOutput.getResult())) {
                            int totalDiff = craftedAmount - block.asItem().getMaxCount();
                            craftedAmount -= Math.max(totalDiff, 0);
                            this.setStack(OUTPUT_SLOT, new ItemStack(block.asItem(), craftedAmount));
                            isOutputSet = true;
                        }
                    }
                }

                if (!isOutputSet) {
                    // Check if a Minecraft item exists of matching name
                    for (Item item : Registries.ITEM) {
                        String itemName = item.getName().getString();

                        if (itemName.equalsIgnoreCase(matchingOutput.getResult())) {
                            int totalDiff = craftedAmount - item.getMaxCount();
                            craftedAmount -= Math.max(totalDiff, 0);
                            this.setStack(OUTPUT_SLOT, new ItemStack(item, craftedAmount));
                            isOutputSet = true;
                        }
                    }
                }

                if (!isOutputSet) {
                    // Create the custom item
                    int totalDiff = craftedAmount - Infinicraft.INFINITE_ITEM.getMaxCount();
                    craftedAmount -= Math.max(totalDiff, 0);

                    var item = JsonHandler.getItemById(matchingOutput.getResult());

                    if (item == null) {
                        // It's possible that this might happen very very rarely due to a race condition vs the executor
                        // thread. We can try again later.
                        isOutputSet = false;
                    } else {
                        ItemStack customItem = new ItemStack(Infinicraft.INFINITE_ITEM, craftedAmount);
                        item.copyNbtToStack(customItem);

                        if (matchingOutput.getInputs() != null)
                            customItem.getOrCreateNbt().putString("recipe", String.join(" + ", matchingOutput.getInputs()));

                        this.setStack(OUTPUT_SLOT, customItem);
                        isOutputSet = true;
                    }
                }

                markDirty(world, pos, state);
                updateInputSlots(inputOne, inputTwo, craftedAmount);
            } else {
                // Add to crafting queue
                if (lastRequest == null || !equalsSortedCaseInsensitive(lastRequest, requestedRecipe)) {
                    lastRequest = requestedRecipe;
                    addToQueue(requestedRecipe);
                }
            }
        } else {
            this.crafting = false;
        }
    }

    private void updateInputSlots(ItemStack stack_one, ItemStack stack_two, int count) {
        this.setStack(INPUT_ONE_SLOT, stack_one.copyWithCount(stack_one.getCount() - count));
        this.setStack(INPUT_TWO_SLOT, stack_two.copyWithCount(stack_two.getCount() - count));
    }

    private void addToQueue(String[] recipe) {
        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        executorService.submit(() -> {
            try {
                processRecipe(recipe, configDir);
            } catch (IOException e) {
                Infinicraft.LOGGER.error("During inference for infinite crafting", e);
            }}
        );
    }

    // Request
    private record OpenAIRequestBody(
            @SerializedName("model") String model,
            @SerializedName("temperature") double temperature,
            @SerializedName("messages") OpenAIMessage... messages
    ) {}
    private record OpenAIMessage(
            @SerializedName("role") String role,
            @SerializedName("content") String content
    ) {}

    // Response
    private record OpenAIResponseBody(
            @SerializedName("id") String id,
            @SerializedName("object") String object,
            @SerializedName("created") long created,
            @SerializedName("model") String model,
            @SerializedName("system_fingerprint") String systemFingerprint,
            @SerializedName("choices") OpenAIResponseChoice[] choices,
            @SerializedName("usage") OpenAIResponseUsage usage
    ) {}
    private record OpenAIResponseChoice(
            @SerializedName("index") int index,
            @SerializedName("message") OpenAIResponseMessage message,
            @SerializedName("logprobs") JsonObject logprobs,
            @SerializedName("finish_reason") String finishReason
    ) {}
    private record OpenAIResponseMessage(
            @SerializedName("role") String role,
            @SerializedName("content") String content
    ) {}
    private record OpenAIResponseUsage(
            @SerializedName("prompt_tokens") int promptTokens,
            @SerializedName("completion_tokens") int completionTokens,
            @SerializedName("total_tokens") int totalTokens
    ) {}

    private void processRecipe(String[] items, String configDir) throws IOException {
        String recipe = String.join(" + ", items);
        Infinicraft.LOGGER.debug("Crafting: {}", recipe);

        String prompt = Infinicraft.CONFIG.PROMPT();

        var requestBody = new OpenAIRequestBody(
                Infinicraft.CONFIG.CHAT_API_MODEL(),
                0.75,
                new OpenAIMessage("system", prompt),
                new OpenAIMessage("user", recipe)
        );

        try {
            var httpRequest = HttpRequest.newBuilder()
                    .POST(GsonBodyPublisher.ofJson(JsonHandler.GSON, requestBody))
                    .uri(new URI(Infinicraft.CONFIG.CHAT_API_BASE()+"/chat/completions"))
                    .setHeader("Authorization", "Bearer " + Infinicraft.CONFIG.CHAT_API_KEY())
                    .setHeader("Content-Type", "application/json; charset=UTF-8")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            var response = httpClient.send(httpRequest, GsonBodyHandler.ofJson(JsonHandler.GSON, OpenAIResponseBody.class));

            int responseCode = response.statusCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected response code: " + responseCode);
            }

            var responseJson = response.body().get();

            String contentString = responseJson.choices()[0].message().content();

            Infinicraft.LOGGER.debug("Response from chat API: {}", contentString);

            // Improve the odds of JSON deserializing correctly
            contentString = massageJson(contentString);

            GeneratedItem generatedItem = JsonHandler.GSON.fromJson(contentString, GeneratedItem.class);

            Infinicraft.LOGGER.debug("Item crafted: {} = {}", recipe, generatedItem.getName());

            updateRecipesFile(items, generatedItem);
            updateItemsFile(generatedItem);
        } catch (Exception e) {
            Infinicraft.LOGGER.error("Error during crafting", e);
        }
    }

    // quirks: allow multiple opening and closing braces, and just slim them down to one
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{+(.*)}+", Pattern.DOTALL);
    private static final Jankson JANKSON = Jankson.builder().allowBareRootObject().build();
    private String massageJson(String contentString) {
        contentString = contentString.trim();

        var matcher = JSON_OBJECT_PATTERN.matcher(contentString);
        if (matcher.find()) {
            contentString = matcher.group(0);
        }

        // Normalize with Jankson
        try {
            contentString = JANKSON.load(contentString).toJson(false, false);
        } catch (SyntaxError ex) {
            Infinicraft.LOGGER.debug("Could not normalize input {} with jankson", contentString, ex);
        }

        return contentString;
    }

    private void updateRecipesFile(String[] ingredients, GeneratedItem generatedItem) throws IOException {
        var recipe = new GeneratedRecipe();
        recipe.setInputs(ingredients);
        recipe.setResult(generatedItem.getName());
        recipe.setResultColor(generatedItem.getColor());
        JsonHandler.saveRecipe(recipe);
    }

    private void updateItemsFile(GeneratedItem generatedItem) {
        if (!JsonHandler.doesItemExist(generatedItem.getName())) {
            JsonHandler.saveItem(generatedItem);
            if(Infinicraft.CONFIG.USE_GENERATOR()) {
                Infinicraft.makeStableDiffusionRequest(generatedItem)
                        .thenAccept(response -> {
                            if (!response.isSuccess()) {
                                Infinicraft.LOGGER.error("SD image generation was unsuccessful for item {}", generatedItem.getName());
                            } else {
                                Infinicraft.LOGGER.debug("SD image generation was OK for item {}", generatedItem.getName());
                                generatedItem.setTexture(response.image());
                                JsonHandler.saveItem(generatedItem);
                            }
                        })
                        .exceptionally(ex -> {
                            Infinicraft.LOGGER.error("Failed to generate SD image for item {}", generatedItem.getName(), ex);
                            return null;
                        });
            }
        }
    }

    private boolean equalsSortedCaseInsensitive(String[] array1, String[] array2) {
        if (array1.length != array2.length) return false;

        if (array1 == array2) return true;

        Arrays.sort(array1, String.CASE_INSENSITIVE_ORDER);
        Arrays.sort(array2, String.CASE_INSENSITIVE_ORDER);

        return Arrays.equals(array1, array2, String.CASE_INSENSITIVE_ORDER);
    }

//    @Override
//    public Object getScreenOpeningData(ServerPlayerEntity player) {
//        return null;
//    }
}
