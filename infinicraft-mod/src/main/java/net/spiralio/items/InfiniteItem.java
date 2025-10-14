package net.spiralio.items;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.spiralio.Infinicraft;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;

public class InfiniteItem extends Item implements FabricItem {

  public InfiniteItem(Settings settings) {
    super(settings);
  }

  @Override
  public String getTranslationKey(ItemStack stack) {
    // returns the item name

    String itemName = "???"; // defaults to ???

    if (stack.hasNbt()) { // get name from NBT tag
      String NBTName =
        stack.getNbt().getString("Name") + stack.getNbt().getString("item");
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
    public <T> Optional<T> visit(
      StringVisitable.StyledVisitor<T> visitor,
      Style style
    ) {
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
    public <T> Optional<T> visit(
      StringVisitable.StyledVisitor<T> visitor,
      Style style
    ) {
      Style newStyle = Style.EMPTY.withColor(Formatting.DARK_GRAY);
      return visitor.accept(newStyle, this.string);
    }

    @Override
    public String toString() {
      return "literal{" + this.string + "}";
    }
  }

  @Override
  public void appendTooltip(
    ItemStack stack,
    @Nullable World world,
    List<Text> tooltip,
    TooltipContext context
  ) {
    if (stack.hasNbt()) { // get name from NBT tag
      String recipeTooltip = stack.getNbt().getString("recipe"); // defaults to ???
      if (!recipeTooltip.isEmpty() && Infinicraft.CONFIG.SHOW_RECIPE()) {
        tooltip.add(MutableText.of(new RecipeLiteral(recipeTooltip)));
      }

      String descriptionTooltip = stack.getNbt().getString("description"); // defaults to ???
      if (
        !descriptionTooltip.isEmpty() && Infinicraft.CONFIG.SHOW_DESCRIPTION()
      ) {
        String[] descriptionLines = descriptionTooltip.split("\n");
        for (String line : descriptionLines) {
          tooltip.add(MutableText.of(new DescriptionLiteral(line)));
        }
      }
    }

    super.appendTooltip(stack, world, tooltip, context);
  }

  @Nullable
  @Override
  public FoodComponent getFoodComponent(ItemStack itemStack) {
    if (!itemStack.hasNbt()) return null;

    String itemName = itemStack.getNbt().getString("item");
    if (itemName == null) return null;

    float nutritionValue = 0;
    if (itemStack.getNbt().contains("nutritionalValue")) {
      nutritionValue = itemStack.getNbt().getFloat("nutritionalValue");
    } else {
      var jsonItem = JsonHandler.getItemById(itemName);
      if (jsonItem != null) {
        nutritionValue = jsonItem.getNutritionalValue();
      }
    }

    boolean isPoisonous = false;
    if (itemStack.getNbt().contains("isPoisonous")) {
      isPoisonous = itemStack.getNbt().getBoolean("isPoisonous");
    } else {
      var jsonItem = JsonHandler.getItemById(itemName);
      if (jsonItem != null) {
        isPoisonous = jsonItem.isPoisonous();
      }
    }

    if (Math.abs(nutritionValue) <= 0.001f && !isPoisonous) return null;

    FoodComponent.Builder foodBuilder = new FoodComponent.Builder();
    if (nutritionValue > 0) {
      foodBuilder.hunger((int) (8 * nutritionValue)); // Positive nutrition restores hunger
    }
    if (isPoisonous) {
      foodBuilder.statusEffect(
        new net.minecraft.entity.effect.StatusEffectInstance(
          net.minecraft.entity.effect.StatusEffects.POISON,
          200,
          1
        ),
        1.0f
      );
    }

    return foodBuilder.build();
  }
}
