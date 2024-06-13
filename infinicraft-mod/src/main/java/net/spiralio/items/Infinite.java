package net.spiralio.items;

import com.google.gson.*;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Infinite extends Item implements FabricItem {

    public Infinite(Settings settings) {
        super(settings);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        // returns the item name

        String itemName = "???"; // defaults to ???

        if (stack.hasNbt()) { // get name from NBT tag
            String NBTName = stack.getNbt().getString("Name") + stack.getNbt().getString("item");
            if (!NBTName.isEmpty()) itemName = NBTName;
        }

        return itemName;
    }

    HashMap<String, Float> storedNutrition = new HashMap<>();
    HashMap<String, Boolean> storedThrowables = new HashMap<>();

    @Nullable
    @Override
    public FoodComponent getFoodComponent(ItemStack itemStack) {
        if (!itemStack.hasNbt()) return null;

        String itemName = itemStack.getNbt().getString("item");
        if (itemName == null) return null;

        if (storedNutrition.containsKey(itemName.toLowerCase())) {


            float nutritionValue = storedNutrition.get(itemName.toLowerCase());

            if (nutritionValue == 0) return null;

            FoodComponent.Builder foodBuilder = new FoodComponent.Builder();
            foodBuilder.hunger((int)(2)); // Calculate the saturation it gives
            System.out.println(nutritionValue);

            return foodBuilder.build();
        } else {
            // No stored nutrition value for this item
            // Read the JSON file to find it
            String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
            String itemsJSONPath = configDir + "/infinicraft/items.json";

            JsonArray items = JsonHandler.readArray(itemsJSONPath, "getting nutrition");

            if (items == null) return null; // Items object failed to load

            // Add all nutrition values into the storage
            // Might as well update all now
            for (int i = 0; i < items.size(); i++) {

                JsonObject thisItem = items.get(i).getAsJsonObject();
                String thisItemName = thisItem.get("item").getAsString();
                JsonElement nutritionElement = thisItem.get("nutritionalValue");

                if (nutritionElement == null) continue;

                float nutritionValue = nutritionElement.getAsFloat();

                storedNutrition.put(thisItemName.toLowerCase(), nutritionValue); // Add to the table
            }

            return null;
        }
    }
}
