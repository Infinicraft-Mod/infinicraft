package net.spiralio.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GeneratedRecipe {
    @SerializedName("input")
    private List<String> inputs = new ArrayList<>();

    @SerializedName("output")
    private String result;

    @SerializedName("color")
    private String resultColor;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs.clear();
        this.inputs.addAll(inputs);
    }

    public void setInputs(String... inputs) {
        this.inputs.clear();
        this.inputs.addAll(Arrays.asList(inputs));
    }

    public String getResultColor() {
        return resultColor;
    }

    public void setResultColor(String resultColor) {
        this.resultColor = resultColor;
    }

    public String getInput(int index) {
        return inputs.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedRecipe that = (GeneratedRecipe) o;
        return Objects.equals(inputs, that.inputs) && Objects.equals(result, that.result) && Objects.equals(resultColor, that.resultColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, result, resultColor);
    }
}
