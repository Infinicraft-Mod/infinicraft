package net.spiralio.util;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;

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

    @SerializedName("isPoisonous")
    private boolean isPoisonous;

    @SerializedName("throwable")
    private boolean isThrowable;

    @SerializedName("attack")
    private float attack;

    @SerializedName("color")
    private String color;

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

    public boolean isPoisonous() {
        return isPoisonous;
    }

    public void setPoisonous(boolean poisonous) {
        isPoisonous = poisonous;
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

    public float getAttack() {
        return attack;
    }

    public void setAttack(float attack) {
        this.attack = attack;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void copyNbtToStack(ItemStack stack) {
        var nbt = stack.getOrCreateNbt();
        nbt.putString("item", getName());
        if (getColor() != null) nbt.putString("color", getColor());
        if (getDescription() != null) nbt.putString("description", getDescription());
        if (getTexture() != null) nbt.putIntArray("texture", getTexture()); // TODO is it too much data to store the texture in the NBT?
        if (getNutritionalValue() > 0) nbt.putFloat("nutritionalValue", getNutritionalValue());
        if (isPoisonous()) nbt.putBoolean("isPoisonous", true);
        if (isThrowable()) nbt.putBoolean("throwable", true);
        if (getAttack() > 0) nbt.putFloat("attack", getAttack());
    }
}
