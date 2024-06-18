package net.spiralio.items;

import com.google.gson.*;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.world.World;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    public record RecipeLiteral(String string) implements PlainTextContent
    {
        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.string);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            Style newStyle = Style.EMPTY.withColor(Color.LIGHT_GRAY.getRGB());
            return visitor.accept(newStyle, this.string);
        }

        @Override
        public String toString() {
            return "literal{" + this.string + "}";
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt()) { // get name from NBT tag
            String newTooltip = stack.getNbt().getString("recipe"); // defaults to ???
            if (!newTooltip.isEmpty()) {
                Text newText = MutableText.of(new RecipeLiteral(newTooltip));
                tooltip.add(newText);
                super.appendTooltip(stack, world, tooltip, context);
                return;
            }
        }

        super.appendTooltip(stack, world, tooltip, context);
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
            foodBuilder.hunger((int)(8*nutritionValue)); // Calculate the saturation it gives

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
