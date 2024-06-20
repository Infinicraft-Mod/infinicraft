package net.spiralio.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.spiralio.Infinicraft;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonHandler {

    private static final Logger LOGGER = Infinicraft.LOGGER;

    public static final Gson GSON =
            new GsonBuilder()
                    .setLenient()
                    // Encodes byte[] as base64
                    .registerTypeAdapter(byte[].class, new TypeAdapter<byte[]>() {
                        private final Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
                        private final Base64.Decoder decoder = Base64.getDecoder();

                        @Override
                        public void write(JsonWriter out, byte[] value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                                return;
                            }

                            out.value(encoder.encodeToString(value));
                        }
                        @Override
                        public byte[] read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                return null;
                            }

                            return decoder.decode(in.nextString());
                        }
                    })
                    // Encodes int[] as base64
                    .registerTypeAdapter(int[].class, new TypeAdapter<int[]>() {
                        private final Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
                        private final Base64.Decoder decoder = Base64.getDecoder();

                        @Override
                        public void write(JsonWriter out, int[] value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                                return;
                            }

                            ByteBuffer byteBuffer = ByteBuffer.allocate(value.length * 4);
                            IntBuffer intBuffer = byteBuffer.asIntBuffer();
                            intBuffer.put(value);

                            out.value(encoder.encodeToString(byteBuffer.array()));
                        }
                        @Override
                        public int[] read(JsonReader in) throws IOException {
                            if (in.peek() == JsonToken.NULL) {
                                return null;
                            }

                            if (in.peek() == JsonToken.STRING) {
                                var bytes = decoder.decode(in.nextString());
                                var byteBuffer = ByteBuffer.wrap(bytes);

                                var intBuffer = byteBuffer.asIntBuffer();
                                int[] arr = new int[intBuffer.remaining()];
                                intBuffer.get(arr);
                                return arr;
                            }

                            if (in.peek() == JsonToken.BEGIN_ARRAY) {
                                in.beginArray();

                                var list = new IntArrayList();
                                while (in.peek() != JsonToken.END_ARRAY) {
                                    list.add(in.nextInt());
                                }
                                in.endArray();

                                return list.toIntArray();
                            }

                            throw new JsonSyntaxException("Did not expect " + in.peek() + " when deserializing b64 int array");
                        }
                    })
                    .create();

    // TODO: replace with something better
    private static final Map<CaseInsensitiveString, GeneratedItem> savedItems = new HashMap<>();
    private static final Map<List<CaseInsensitiveString>, GeneratedRecipe> savedRecipes = new HashMap<>();

    // items.json last modified date
    private static volatile long lastModified;

    // recipes.json last modified date
    private static volatile long recipesLastModified;

    // For the rare occurrence in which items.json might be read by multiple threads at once
    private static final Object itemsJsonLock = new Object();
    private static final Object recipesJsonLock = new Object();

    private static Path getItemsJsonPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("infinicraft/items.json");
    }
    private static Path getRecipesJsonPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("infinicraft/recipes.json");
    }

    @Nullable
    public static GeneratedItem getItemById(String id) {
        try {
            refreshSavedItems(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return savedItems.get(new CaseInsensitiveString(id));
    }

    private static void refreshSavedItems(boolean replaceIfPresent) throws IOException {
        var itemsJsonPath = getItemsJsonPath();

        if (!Files.exists(itemsJsonPath)) return;

        long newLastModified = Files.getLastModifiedTime(itemsJsonPath).toMillis();
        if (savedItems.isEmpty() || lastModified != newLastModified) {
            synchronized (itemsJsonLock) {
                // We need to check again inside the synchronized block as we may have waited for another version of
                // this method to execute.
                if (lastModified == newLastModified) {
                    return;
                }

                try (var reader = Files.newBufferedReader(itemsJsonPath)) {
                    var arr = GSON.fromJson(reader, GeneratedItem[].class);
                    for (GeneratedItem savedItemData : arr) {
                        if (replaceIfPresent) {
                            savedItems.put(new CaseInsensitiveString(savedItemData.getName()), savedItemData);
                        } else {
                            savedItems.putIfAbsent(new CaseInsensitiveString(savedItemData.getName()), savedItemData);
                        }
                    }
                    lastModified = newLastModified;
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Got an EOF error", e);
                }
            }
        }
    }

    private static void refreshSavedRecipes(boolean replaceIfPresent) throws IOException {
        var recipesJsonPath = getRecipesJsonPath();

        if (!Files.exists(recipesJsonPath)) return;

        long newLastModified = Files.getLastModifiedTime(recipesJsonPath).toMillis();
        if (savedRecipes.isEmpty() || recipesLastModified != newLastModified) {
            synchronized (recipesJsonLock) {
                // We need to check again inside the synchronized block as we may have waited for another version of
                // this method to execute.
                if (recipesLastModified == newLastModified) {
                    return;
                }

                try (var reader = Files.newBufferedReader(recipesJsonPath)) {
                    var arr = GSON.fromJson(reader, GeneratedRecipe[].class);
                    for (GeneratedRecipe savedRecipeData : arr) {
                        if (replaceIfPresent) {
                            savedRecipes.put(massageInputs(savedRecipeData.getInputs()), savedRecipeData);
                        } else {
                            savedRecipes.putIfAbsent(massageInputs(savedRecipeData.getInputs()), savedRecipeData);
                        }
                    }
                    recipesLastModified = newLastModified;
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Got an EOF error", e);
                }
            }
        }
    }

    public static void saveItem(GeneratedItem itemData) {
        // Update or add our item
        savedItems.put(new CaseInsensitiveString(itemData.getName()), itemData);

        // Read new items from items.json, but do not replace existing ones.
        var itemsJsonPath = getItemsJsonPath();
        try {
            refreshSavedItems(false);

            synchronized (itemsJsonLock) {
                try (var writer = Files.newBufferedWriter(itemsJsonPath)) {
                    GSON.toJson(savedItems.values().toArray(GeneratedItem[]::new), GeneratedItem[].class, writer);

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                lastModified = Files.getLastModifiedTime(itemsJsonPath).toMillis();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<CaseInsensitiveString> massageInputs(Collection<String> inputs) {
        var list = new ArrayList<CaseInsensitiveString>(inputs.size());
        for (String input : inputs) {
            list.add(new CaseInsensitiveString(input));
        }
        list.sort(CaseInsensitiveString.COMPARATOR);
        return list;
    }

    private static List<CaseInsensitiveString> massageInputs(String... inputs) {
        var list = new ArrayList<CaseInsensitiveString>(inputs.length);
        for (String input : inputs) {
            list.add(new CaseInsensitiveString(input));
        }
        list.sort(CaseInsensitiveString.COMPARATOR);
        return list;
    }

    public static void saveRecipe(GeneratedRecipe recipeData) {
        // Update or add our recipe
        savedRecipes.put(massageInputs(recipeData.getInputs()), recipeData);

        // Read new recipes from recipes.json, but do not replace existing ones.
        var recipesJsonPath = getRecipesJsonPath();
        try {
            refreshSavedRecipes(false);

            synchronized (recipesJsonLock) {
                try (var writer = Files.newBufferedWriter(recipesJsonPath)) {
                    GSON.toJson(savedRecipes.values().toArray(GeneratedRecipe[]::new), GeneratedRecipe[].class, writer);

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                recipesLastModified = Files.getLastModifiedTime(recipesJsonPath).toMillis();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<GeneratedItem> getItems() {
        return savedItems.values();
    }

    public static boolean doesItemExist(String itemName) {
        return getItemById(itemName) != null;
    }

    @Nullable
    public static GeneratedRecipe getRecipe(String... requestedIngredients) {
        try {
            refreshSavedRecipes(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return savedRecipes.get(massageInputs(requestedIngredients));
    }
}
