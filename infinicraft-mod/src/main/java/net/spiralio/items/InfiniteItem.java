package net.spiralio.items;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InfiniteItem extends Item implements FabricItem {

    public InfiniteItem(Settings settings) {
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

    public record RecipeLiteral(String string) implements PlainTextContent {
        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.string);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            Style newStyle = Style.EMPTY.withColor(Formatting.GRAY);
            return visitor.accept(newStyle, this.string);
        }

        @Override
        public String toString() {
            return "literal{" + this.string + "}";
        }
    }

    public record DescriptionLiteral(String string) implements PlainTextContent {
        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.string);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            Style newStyle = Style.EMPTY.withColor(Formatting.DARK_GRAY);
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
            String recipeTooltip = stack.getNbt().getString("recipe"); // defaults to ???
            if (!recipeTooltip.isEmpty()) {
                tooltip.add(MutableText.of(new RecipeLiteral(recipeTooltip)));
            }

            String descriptionTooltip = stack.getNbt().getString("description"); // defaults to ???
            if (!descriptionTooltip.isEmpty()) {
                tooltip.add(MutableText.of(new DescriptionLiteral(descriptionTooltip)));
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
        Float nutritionValue = storedNutrition.getOrDefault(itemName.toLowerCase(Locale.ROOT), null);
        if (nutritionValue == null) {
            // No stored nutrition value for this item
            // Read the JSON file to find it

            var item = JsonHandler.getItemById(itemName);
            if (item != null) {
                storedNutrition.put(item.getName().toLowerCase(Locale.ROOT), nutritionValue = item.getNutritionalValue()); // Add to the table
            }
        }

        if (nutritionValue == null || Math.abs(nutritionValue) <= 0.001f) return null;

        FoodComponent.Builder foodBuilder = new FoodComponent.Builder();
        foodBuilder.hunger((int)(8*nutritionValue)); // Calculate the saturation it gives

        return foodBuilder.build();
    }
}
