package net.spiralio.blocks.entity;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InfinicrafterEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    private static final int INPUT_ONE_SLOT = 0;
    private static final int INPUT_TWO_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    protected final PropertyDelegate propertyDelegate;
    private boolean crafting = false;

    public InfinicrafterEntity(BlockPos pos, BlockState state) {
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
        ItemStack input_one = this.getStack(INPUT_ONE_SLOT);
        ItemStack input_two = this.getStack(INPUT_TWO_SLOT);
        if (!input_one.isEmpty() && !input_two.isEmpty()) {
            // Time to craft!

            this.crafting = true;

            // Store inputs in a recipe array
            String[] requestedRecipe = {
                    input_one.getName().getString(),
                    input_two.getName().getString(),
            };

            int requestedCount = Math.min(input_one.getCount(), input_two.getCount());

            // Read recipes file
            JsonArray recipes = getRecipes();

            if (recipes == null) return;

            // Check if a matching recipe exists
            String[] matchingOutput = getMatchingOutput(requestedRecipe, recipes);

            if (matchingOutput != null) {
                // Recipe found
                boolean isOutputSet = false;
                if (!output.isEmpty()) {
                    // don't generate place output in block output slot if output slot is not empty
                    // and the new output doesn't match the item already in that slot
                    if (!output.getName().getString().equalsIgnoreCase(matchingOutput[0])) {
                        return;
                    } else {
                        int totalDiff = output.getCount() + requestedCount - output.getMaxCount();
                        requestedCount -= Math.max(totalDiff, 0);
                        this.setStack(OUTPUT_SLOT, output.copyWithCount(output.getCount() + requestedCount));
                        isOutputSet = true;
                    }
                }

                if (!isOutputSet) {
                    // Check if a Minecraft block exists of matching name
                    for (Block block : Registries.BLOCK) {
                        String blockName = block.getName().getString();

                        if (blockName.equalsIgnoreCase(matchingOutput[0])) {
                            int totalDiff = requestedCount - block.asItem().getMaxCount();
                            requestedCount -= Math.max(totalDiff, 0);
                            this.setStack(OUTPUT_SLOT, new ItemStack(block.asItem(), requestedCount));
                            isOutputSet = true;
                        }
                    }
                }

                if (!isOutputSet) {
                    // Check if a Minecraft item exists of matching name
                    for (Item item : Registries.ITEM) {
                        String itemName = item.getName().getString();

                        if (itemName.equalsIgnoreCase(matchingOutput[0])) {
                            int totalDiff = requestedCount - item.getMaxCount();
                            requestedCount -= Math.max(totalDiff, 0);
                            this.setStack(OUTPUT_SLOT, new ItemStack(item, requestedCount));
                            isOutputSet = true;
                        }
                    }
                }

                if (!isOutputSet) {
                    // Create the custom item
                    int totalDiff = requestedCount - Infinicraft.INFINITE.getMaxCount();
                    requestedCount -= Math.max(totalDiff, 0);
                    ItemStack customItem = new ItemStack(Infinicraft.INFINITE, requestedCount);
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("item", matchingOutput[0]);
                    if (matchingOutput[1] != null) nbt.putString("color", matchingOutput[1]);
                    if (matchingOutput[2] != null) nbt.putString("recipe", matchingOutput[2]);
                    customItem.setNbt(nbt);

                    this.setStack(OUTPUT_SLOT, customItem);
                    isOutputSet = true;
                }
                markDirty(world, pos, state);
                updateInputSlots(input_one, input_two, requestedCount);
            } else {
                // Add to crafting queue
                if (lastRequest == null || !matchingArraysLowercase(lastRequest, requestedRecipe)) {
                    lastRequest = requestedRecipe.clone();
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

    @Nullable
    private String[] getMatchingOutput(String[] requestedRecipe, JsonArray recipes) {
        String[] matchingOutput = null;
        Arrays.sort(requestedRecipe);

        // Iterate through each recipe
        for (int i = 0; i < recipes.size(); i++) {

            JsonObject thisRecipe = recipes.get(i).getAsJsonObject();

            JsonArray inputs = thisRecipe.get("input").getAsJsonArray();
            String output = thisRecipe.get("output").getAsString();

            String color = null;
            JsonElement colorElement = thisRecipe.get("color");
            if (colorElement != null) color = colorElement.getAsString();

            // Create a string array with all elements of the recipe
            String[] inputStringArray = new String[inputs.size()];
            for (int j = 0; j < inputs.size(); j++) {
                inputStringArray[j] = inputs.get(j).getAsString();
            }
            String recipe = String.join(" + ", inputStringArray);

            // Check if all elements are the same
            if (matchingArraysLowercase(inputStringArray, requestedRecipe)) {
                matchingOutput = new String[]{output, color, recipe};
                break;
            }
        }

        return matchingOutput;
    }

    private JsonArray getRecipes() {
        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        String recipesJSONPath = configDir + "/infinicraft/recipes.json";

        Gson gson = new Gson();
        File recipesFile = new File(recipesJSONPath);
        File recipesDir = recipesFile.getParentFile();

        // Ensure the directory exists
        if (!recipesDir.exists()) {
            recipesDir.mkdirs();
        }

        // Ensure the file exists and create it with an empty JSON array if it does not
        if (!recipesFile.exists()) {
            try (FileWriter writer = new FileWriter(recipesFile)) {
                writer.write("[]");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create recipes.json", e);
            }
        }

        // Read and parse the JSON file
        try (FileReader reader = new FileReader(recipesFile)) {
            return gson.fromJson(reader, JsonArray.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("recipes.json file not found", e);
        } catch (IOException | JsonSyntaxException e) {
            throw new RuntimeException("Failed to read or parse recipes.json", e);
        }
    }

    private JsonArray getItemsData() {
        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        String itemsJSONPath = configDir + "/infinicraft/items.json";

        Gson gson = new Gson();
        File itemsFile = new File(itemsJSONPath);
        File itemsDir = itemsFile.getParentFile();

        // Ensure the directory exists
        if (!itemsDir.exists()) {
            itemsDir.mkdirs();
        }

        // Ensure the file exists and create it with an empty JSON array if it does not
        if (!itemsFile.exists()) {
            try (FileWriter writer = new FileWriter(itemsFile)) {
                writer.write("[]");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create items.json", e);
            }
        }

        // Read and parse the JSON file
        try (FileReader reader = new FileReader(itemsFile)) {
            return gson.fromJson(reader, JsonArray.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("items.json file not found", e);
        } catch (IOException | JsonSyntaxException e) {
            throw new RuntimeException("Failed to read or parse items.json", e);
        }
    }

    private void addToQueue(String[] recipe) throws IOException {
        Gson gson = new Gson();
        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        executorService.submit(() -> {
            try {
                processRecipe(recipe, gson, configDir);
            } catch (IOException e) {
                e.printStackTrace();
            }});
    }

    private void processRecipe(String[] items, Gson gson, String configDir) throws IOException {
        String recipe = String.join(" + ", items);
        System.out.println("Crafting: " + recipe);

        String prompt = Infinicraft.CONFIG.PROMPT();

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", prompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", recipe);
        messages.add(userMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", Infinicraft.CONFIG.CHAT_API_MODEL());
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.75);

        try {
            URL url = new URL(Infinicraft.CONFIG.CHAT_API_BASE()+"/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + Infinicraft.CONFIG.CHAT_API_KEY());
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected response code: " + responseCode);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JsonObject responseJson = gson.fromJson(response.toString(), JsonObject.class);
                String contentString = responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
                JsonObject output = gson.fromJson(contentString, JsonObject.class);

                String itemName = output.get("item").getAsString();
                String itemColor = output.get("color").getAsString();

                System.out.println("Item crafted: " + recipe + " = " + itemName);

                updateRecipesFile(items, itemName, itemColor, gson, configDir);
                updateItemsFile(output, itemName, gson, configDir);
            }
        } catch (Exception e) {
            System.out.println("Error during crafting: " + e.getMessage());
        }
    }

    private void updateRecipesFile(String[] items, String itemName, String itemColor, Gson gson, String configDir) throws IOException {
        String recipesPath = configDir + "/infinicraft/recipes.json";
        File recipesFile = new File(recipesPath);
        JsonArray recipes;

        if (recipesFile.exists()) {
            try (FileReader reader = new FileReader(recipesFile)) {
                recipes = gson.fromJson(reader, JsonArray.class);
            }
        } else {
            recipes = new JsonArray();
        }

        JsonObject newRecipe = new JsonObject();
        newRecipe.add("input", gson.toJsonTree(items));
        newRecipe.addProperty("output", itemName);
        newRecipe.addProperty("color", itemColor);
        recipes.add(newRecipe);

        try (FileWriter writer = new FileWriter(recipesPath)) {
            gson.toJson(recipes, writer);
        }
    }

    private void updateItemsFile(JsonObject output, String itemName, Gson gson, String configDir) throws IOException {
        String itemsPath = configDir + "/infinicraft/items.json";
        File itemsFile = new File(itemsPath);
        JsonArray items;

        if (itemsFile.exists()) {
            try (FileReader reader = new FileReader(itemsFile)) {
                items = gson.fromJson(reader, JsonArray.class);
            }
        } else {
            items = new JsonArray();
        }

        for (JsonElement itemElement : items) {
            JsonObject itemObject = itemElement.getAsJsonObject();
            if (itemObject.get("item").getAsString().equals(itemName)) {
                return;
            }
        }

        items.add(output);

        try (FileWriter writer = new FileWriter(itemsPath)) {
            gson.toJson(items, writer);
        }
    }

    private boolean matchingArraysLowercase(String[] array1, String[] array2) {
        if (array1.length != array2.length) return false;

        Arrays.sort(array1);
        Arrays.sort(array2);

        for (int j = 0; j < array1.length; j++) {
            String string1 = array1[j].toLowerCase();
            String string2 = array2[j].toLowerCase();
            if (!string1.equals(string2)) {
                return false;
            }
        }

        return true;
    }

//    @Override
//    public Object getScreenOpeningData(ServerPlayerEntity player) {
//        return null;
//    }
}
