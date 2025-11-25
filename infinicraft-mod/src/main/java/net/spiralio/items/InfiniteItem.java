package net.spiralio.items;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents;
import net.spiralio.Infinicraft;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.registry.tag.BlockTags;

public class InfiniteItem extends Item {

  public InfiniteItem(Settings settings) {
    super(settings);
  }

  @Override
  public Text getName(ItemStack stack) {
    String itemName = "???"; // defaults to ???

    if (stack.hasNbt()) { // get name from NBT tag
      String NBTName = stack.getNbt().getString("Name") + stack.getNbt().getString("item");
      if (!NBTName.isEmpty())
        itemName = NBTName;
    }

    Formatting color = Formatting.WHITE;
    if (stack.hasNbt() && stack.getNbt().contains("rarity")) {
      String r = stack.getNbt().getString("rarity").toLowerCase(Locale.ROOT);
      switch (r) {
        case "uncommon":
          color = Formatting.YELLOW;
          break;
        case "rare":
          color = Formatting.AQUA;
          break;
        case "epic":
          color = Formatting.LIGHT_PURPLE;
          break;
        default:
          color = Formatting.WHITE;
      }
    }

    return Text.literal(itemName).formatted(color);
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

    if (context.isAdvanced() && stack.hasNbt() && stack.getNbt().contains("durability")) {
      int max = stack.getNbt().getInt("durability");
      int cur = max - stack.getDamage();
      tooltip.add(Text.literal(""));
      tooltip.add(Text.literal("Durability: " + cur + " / " + max).formatted(Formatting.GRAY));
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

    if (Math.abs(nutritionValue) <= 0.001f) return null;

    FoodComponent.Builder foodBuilder = new FoodComponent.Builder();
    foodBuilder.hunger((int) (8 * nutritionValue)); // Calculate the saturation it gives

    return foodBuilder.build();
  }

  @Override
  public boolean isSuitableFor(ItemStack stack, BlockState state) {
    if (!stack.hasNbt())
      return false;

    String toolType = stack.getNbt().getString("toolType");
    int miningLevel = stack.getNbt().getInt("miningLevel"); // 0=Wood, 1=Stone, 2=Iron, 3=Diamond, 4=Netherite

    if (toolType.equalsIgnoreCase("none"))
      return false;

    boolean isPickaxe = state.isIn(BlockTags.PICKAXE_MINEABLE);
    boolean isAxe = state.isIn(BlockTags.AXE_MINEABLE);
    boolean isShovel = state.isIn(BlockTags.SHOVEL_MINEABLE);
    boolean isHoe = state.isIn(BlockTags.HOE_MINEABLE);
    if (!isPickaxe && !isAxe && !isShovel && !isHoe)
      return true;

    boolean typeMatches = false;
    switch (toolType.toLowerCase()) {
      case "pickaxe":
        typeMatches = isPickaxe;
        break;
      case "axe":
        typeMatches = isAxe;
        break;
      case "shovel":
        typeMatches = isShovel;
        break;
      case "hoe":
        typeMatches = isHoe;
        break;
    }

    if (!typeMatches)
      return false;

    if (isPickaxe) {
      if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && miningLevel < 3)
        return false;
      if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && miningLevel < 2)
        return false;
      if (state.isIn(BlockTags.NEEDS_STONE_TOOL) && miningLevel < 1)
        return false;
    }

    return true;
  }

  @Override
  public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
    float baseSpeed = 1.0f;
    if (stack.hasNbt() && stack.getNbt().contains("miningSpeed")) {
      baseSpeed = stack.getNbt().getFloat("miningSpeed");
    }

    return canMineBlock(stack, state) ? baseSpeed : 1.0f;
  }

  private void damageItemStack(ItemStack stack, int amount, LivingEntity user) {
    var nbt = stack.getOrCreateNbt();
    if (!nbt.contains("durability"))
      return;

    int max = nbt.getInt("durability");

    stack.damage(amount, user, e -> {});

    // Check if item should break based on custom max durability
    if (stack.getDamage() >= max) {
      stack.decrement(1);
      if (user != null)
        user.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
    }
  }

  @Override
  public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    damageItemStack(stack, 1, attacker);
    return super.postHit(stack, target, attacker);
  }

  @Override
  public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
    if (!world.isClient) {
      damageItemStack(stack, 1, miner);
    }
    return true;
  }

  @Override
  public boolean isItemBarVisible(ItemStack stack) {
    if (!stack.hasNbt())
      return false;
    var nbt = stack.getNbt();
    if (!nbt.contains("durability"))
      return false;
    int max = nbt.getInt("durability");
    return stack.isDamaged() && stack.getDamage() < max;
  }

  @Override
  public int getItemBarStep(ItemStack stack) {
    if (!stack.hasNbt())
      return 0;
    var nbt = stack.getNbt();
    if (!nbt.contains("durability"))
      return 0;
    int max = nbt.getInt("durability");
    int cur = max - stack.getDamage();
    if (max <= 0)
      return 0;
    int barWidth = 13; // vanilla uses 13 pixels
    int step = Math.round((float) cur / (float) max * barWidth);
    if (step < 0)
      step = 0;
    if (step > barWidth)
      step = barWidth;
    return step;
  }

  @Override
  public int getItemBarColor(ItemStack stack) {
    if (!stack.hasNbt())
      return super.getItemBarColor(stack);
    var nbt = stack.getNbt();
    if (!nbt.contains("durability"))
      return super.getItemBarColor(stack);
    int max = nbt.getInt("durability");
    int cur = max - stack.getDamage();
    float f = Math.max(0.0F, (float) cur / (float) max);
    return net.minecraft.util.math.MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
  }

  public static boolean canMineBlock(ItemStack stack, BlockState state) {
    String toolType = stack.hasNbt() && stack.getNbt().contains("toolType")
        ? stack.getNbt().getString("toolType")
        : "none";

    if (toolType.equalsIgnoreCase("none"))
      return false;

    switch (toolType.toLowerCase()) {
      case "pickaxe":
        return state.isIn(BlockTags.PICKAXE_MINEABLE);
      case "shovel":
        return state.isIn(BlockTags.SHOVEL_MINEABLE);
      case "axe":
        return state.isIn(BlockTags.AXE_MINEABLE);
      case "hoe":
        return state.isIn(BlockTags.HOE_MINEABLE);
      case "sword":
        return state.isIn(BlockTags.SWORD_EFFICIENT);
      default:
        return false;
    }
  }
}
