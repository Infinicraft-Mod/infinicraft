package net.spiralio.util;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import java.util.UUID;

public class GeneratedItem {
    @SerializedName("item")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("texture")
    private int[] texture;

    @SerializedName("custom")
    private boolean isCustom;

    @SerializedName("nutritionalValue")
    private float nutritionalValue;

    @SerializedName("throwable")
    private boolean isThrowable;

    @SerializedName("color")
    private String color;

    @SerializedName("toolType")
    private String toolType;

    @SerializedName("attackSpeed")
    private float attackSpeed;

    @SerializedName("attackDamage")
    private float attackDamage;

    @SerializedName("durability")
    private int durability;

    @SerializedName("rarity")
    private String rarity;

    @SerializedName("miningSpeed")
    private float miningSpeed;

    @SerializedName("miningLevel")
    private int miningLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getTexture() {
        return texture;
    }

    public void setTexture(int[] texture) {
        this.texture = texture;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public float getNutritionalValue() {
        return nutritionalValue;
    }

    public void setNutritionalValue(float nutritionalValue) {
        this.nutritionalValue = nutritionalValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isThrowable() {
        return isThrowable;
    }

    public void setThrowable(boolean throwable) {
        isThrowable = throwable;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public float getMiningSpeed() {
        return miningSpeed;
    }

    public void setMiningSpeed(float miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public void setMiningLevel(int miningLevel) {
        this.miningLevel = miningLevel;
    }

    public void copyNbtToStack(ItemStack stack) {
        var nbt = stack.getOrCreateNbt();
        nbt.putString("item", getName());
        if (getColor() != null) nbt.putString("color", getColor());
        if (getDescription() != null) nbt.putString("description", getDescription());
        if (getTexture() != null) nbt.putIntArray("texture", getTexture()); // TODO is it too much data to store the texture in the NBT?
        if (getNutritionalValue() > 0) nbt.putFloat("nutritionalValue", getNutritionalValue());
        if (isThrowable()) nbt.putBoolean("throwable", true);
        if (getRarity() != null) nbt.putString("rarity", getRarity());
        if (getToolType() != null && !getToolType().equalsIgnoreCase("none")) {
            nbt.putString("toolType", getToolType());
            if (getMiningSpeed() != 0f) nbt.putFloat("miningSpeed", getMiningSpeed());
            if (getMiningLevel() > 0) nbt.putInt("miningLevel", getMiningLevel());            if (getDurability() > 0) nbt.putInt("durability", getDurability());
            if (getName() != null && ((getAttackDamage() >= -1 && getAttackDamage() != 0) || getAttackSpeed() != 0)) {
                NbtList attributeModifiers = new NbtList();
                if (getAttackDamage() >= -1 && getAttackDamage() != 0) {
                    NbtCompound damageModifier = new NbtCompound();
                    damageModifier.putString("AttributeName", "generic.attack_damage");
                    damageModifier.putString("Name", "generic.attack_damage");
                    damageModifier.putDouble("Amount", getAttackDamage());
                    damageModifier.putInt("Operation", 0); // 0 = ADDITION
                    damageModifier.putUuid("UUID",
                            UUID.nameUUIDFromBytes(("InfiniteItem-attackDamage-" + getName()).getBytes()));
                    damageModifier.putString("Slot", "mainhand");
                    attributeModifiers.add(damageModifier);
                }
                if (getAttackSpeed() != 0) {
                    NbtCompound speedModifier = new NbtCompound();
                    speedModifier.putString("AttributeName", "generic.attack_speed");
                    speedModifier.putString("Name", "generic.attack_speed");
                    speedModifier.putDouble("Amount", getAttackSpeed());
                    speedModifier.putInt("Operation", 0); // 0 = ADDITION
                    speedModifier.putUuid("UUID",
                            UUID.nameUUIDFromBytes(("InfiniteItem-attackSpeed-" + getName()).getBytes()));
                    speedModifier.putString("Slot", "mainhand");
                    attributeModifiers.add(speedModifier);
                }
                nbt.put("AttributeModifiers", attributeModifiers);
            }
        }
    }
}
