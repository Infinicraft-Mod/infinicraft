package net.spiralio.blocks.entity;

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
import java.util.Arrays;

public class InfinicrafterEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    private static final int INPUT_ONE_SLOT = 0;
    private static final int INPUT_TWO_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

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

        if (!this.getStack(INPUT_ONE_SLOT).isEmpty() && !this.getStack(INPUT_TWO_SLOT).isEmpty()) {
            // Time to craft!

            this.crafting = true;

            // Store inputs in a recipe array
            String[] requestedRecipe = {
                    this.getStack(INPUT_ONE_SLOT).getName().getString(),
                    this.getStack(INPUT_TWO_SLOT).getName().getString(),
            };

            // Read recipes file
            JsonArray recipes = getRecipes();

            if (recipes == null) return;

            // Check if a matching recipe exists
            String[] matchingOutput = getMatchingOutput(requestedRecipe, recipes);

            if (matchingOutput != null) {
                // Recipe found
                // Remove two slots
                this.removeStack(INPUT_ONE_SLOT);
                this.removeStack(INPUT_TWO_SLOT);

                // Check if a Minecraft block exists of matching name
                for (Block block : Registries.BLOCK) {
                    String blockName = block.getName().getString();

                    if (blockName.equalsIgnoreCase(matchingOutput[0])) {
                        this.setStack(OUTPUT_SLOT, new ItemStack(block.asItem()));
                        markDirty(world, pos, state);
                        return;
                    }
                }

                // Check if a Minecraft item exists of matching name
                for (Item item : Registries.ITEM) {
                    String itemName = item.getName().getString();

                    if (itemName.equalsIgnoreCase(matchingOutput[0])) {
                        this.setStack(OUTPUT_SLOT, new ItemStack(item));
                        markDirty(world, pos, state);
                        return;
                    }
                }

                // Create the custom item
                ItemStack customItem = new ItemStack(Infinicraft.INFINITE);

                NbtCompound nbt = new NbtCompound();
                nbt.putString("item", matchingOutput[0]);
                if (matchingOutput[1] != null) nbt.putString("color", matchingOutput[1]);
                customItem.setNbt(nbt);

                this.setStack(OUTPUT_SLOT, customItem);
                markDirty(world, pos, state);
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

            // Check if all elements are the same
            if (matchingArraysLowercase(inputStringArray, requestedRecipe)) {
                matchingOutput = new String[]{output, color};
                break;
            }
        }

        return matchingOutput;
    }

    private JsonArray getRecipes() {
        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        String recipesJSONPath = configDir + "/infinicraft/recipes.json";

        Gson gson = new Gson();

        FileReader reader = null;
        try {
            reader = new FileReader(new File(recipesJSONPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return gson.fromJson(reader, JsonArray.class);
    }

    private void addToQueue(String[] recipe) throws IOException {
        System.out.println(FabricLoader.getInstance().getConfigDir());

        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        String queueJSONPath = configDir + "/infinicraft/craftQueue.json";
        Gson gson = new Gson();

        FileReader reader = null;
        try {
            reader = new FileReader(new File(queueJSONPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonArray queue = gson.fromJson(reader, JsonArray.class);

        if (queue != null) {
            JsonArray jsonRecipe = new JsonArray();
            for (String s : recipe) {
                jsonRecipe.add(s);
            }

            queue.add(jsonRecipe);

            FileWriter writer = new FileWriter(queueJSONPath);
            gson.toJson(queue, writer);
            writer.flush();
            writer.close();
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
