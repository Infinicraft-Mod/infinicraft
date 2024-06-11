package net.spiralio.models;

import com.google.gson.*;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class InfiniteModel implements UnbakedModel, BakedModel, FabricBakedModel {

    private static final SpriteIdentifier SPRITE_ID = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("infinicraft:block/white")
    );

    private Sprite sprite = null;
    private Sprite stone = null;
    private Mesh mesh;

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        // Don't need because we use FabricBakedModel instead. However, it's better to not return null in case some mod decides to call this function.
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true; // we want the block to have a shadow depending on the adjacent blocks
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return stone; // Block break particle, let's use furnace_top
    }


    private static Transformation makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        Vector3f translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation.set(MathHelper.clamp(translation.x, -5.0F, 5.0F), MathHelper.clamp(translation.y, -5.0F, 5.0F), MathHelper.clamp(translation.z, -5.0F, 5.0F));
        return new Transformation(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    @Override
    public ModelTransformation getTransformation() {

        final Transformation TRANSFORM_BLOCK_GUI = makeTransform(0, 0, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
        final Transformation TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 2, 0, 0.5f, 0.5f, 0.5f);
        final Transformation TRANSFORM_BLOCK_FIXED = makeTransform(0, 180.0f, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
        final Transformation TRANSFORM_BLOCK_3RD_PERSON = makeTransform(0, 0, 0, 0, 3.0f, 1.0f, 0.55f, 0.55f, 0.55f);
        final Transformation TRANSFORM_BLOCK_1ST_PERSON = makeTransform(0, -90.0f, 25.0f, 1.13f, 3.2f, 1.13f, 0.68f, 0.68f, 0.68f);
        final Transformation TRANSFORM_BLOCK_HEAD = makeTransform(0, 180.0f, 0, 0, 13.0f, 7.0f, 1.0f, 1.0f, 1.0f);

        return new ModelTransformation(TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_HEAD, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(); // This model does not depend on other models.
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        // This is related to model parents, it's not required for our use case
    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        // Get the sprites
        sprite = textureGetter.apply(SPRITE_ID);
        stone = textureGetter.apply(new SpriteIdentifier(
                SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:block/brown_mushroom_block")
        ));

        return this;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false; // False to trigger FabricBakedModel rendering
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext context) {
//        mesh.outputTo(context.getEmitter());
    }

    HashMap<String, int[][]> storedTextures = new HashMap<>();
    int tickCount = 0;

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext context) {

        tickCount++;
        if (tickCount > 20) tickCount = 0;

        int[][] pixelGrid = null;
        String thisColor = null;

        if (itemStack.hasNbt()) {
            // If the item has an item ID
            String itemID = itemStack.getNbt().getString("item");
            thisColor = itemStack.getNbt().getString("color");

            // Try to get a texture for this item ID
            if (!itemID.isEmpty()) {
                if (!storedTextures.containsKey(itemID.toLowerCase()) && (tickCount % 10 == 0)) {
                    // Store item texture
                    String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
                    String itemsJSONPath = configDir + "/infinicraft/items.json";
                    JsonArray items = JsonHandler.readArray(itemsJSONPath, "store texture");

                    if (items != null) {
                        // Find a matching item
                        for (int i = 0; i < items.size(); i++) {
                            JsonObject thisItem = items.get(i).getAsJsonObject();

                            String thisItemName = thisItem.get("item").getAsString();
                            JsonElement textureElement = thisItem.get("texture");

                            if (!thisItemName.equalsIgnoreCase(itemID)) continue;

                            // No texture in this object
                            if (textureElement == null) {
                                // Mark as needing a texture (since it has an ID)
                                markCustom(itemID);
                            } else {
                                JsonArray textureArray = textureElement.getAsJsonArray();

                                // Generate a pixel grid for this texture
                                int[][] thisPixelGrid = new int[16][16];

                                for (int row = 0; row < textureArray.size(); row++) {
                                    JsonArray thisRow = textureArray.get(row).getAsJsonArray();

                                    for (int col = 0; col < thisRow.size(); col++) {
                                        thisPixelGrid[row][col] = thisRow.get(col).getAsInt();
                                    }
                                }

                                storedTextures.put(itemID.toLowerCase(), thisPixelGrid);
                            }
                        }
                    }
                }

                // Use stored texture
                if (storedTextures.containsKey(itemID.toLowerCase())) {

                    // Texture found, set it
                    pixelGrid = storedTextures.get(itemID.toLowerCase());

                }
            }

            // Texture NBT
            final NbtList texture = itemStack.getNbt().getList("Texture", 9);

            if (!texture.isEmpty()) {
                pixelGrid = new int[16][16];

                for (int i = 0; i < texture.size(); i++) {
                    NbtList row = texture.getList(i);
                    for (int j = 0; j < row.size(); j++) {
                        pixelGrid[i][j] = row.getInt(j);
                    }
                }
            }
        }

        if (pixelGrid == null) {
            pixelGrid = MissingTextures.getColor(thisColor);
        }

        renderModel(context, pixelGrid);
    }

    private void renderModel(RenderContext context, int[][] pixelGrid) {
        QuadEmitter emitter = context.getEmitter();

        int rowSize = 16;
        int colSize = 16;

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder builder = renderer.meshBuilder();

        float px = 0.0625f;

        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {

                final int color = pixelGrid[row][col];
                if (color == -1) continue;

                for (Direction direction: Direction.values()) {
                    if (direction.equals(Direction.NORTH)) {
                        emitter.square(direction, 1.0f - (col + 1) * px, 1.0f - (row + 1) * px, 1.0f - col * px, 1.0f - row * px, (15 * px)/2);
                    } else if (direction.equals(Direction.SOUTH)) {
                        emitter.square(direction, col * px, 1.0f - (row + 1) * px, (col + 1) * px, 1.0f - row * px, (1.0f - px)/2);
                    } else if (direction.equals(Direction.EAST)) {
                        if (col != (colSize - 1) && pixelGrid[row][col + 1] != -1) continue; // cull face
                        emitter.square(direction, (1.0f - px)/2, 1.0f - (row + 1) * px, (1.0f + px)/2, 1.0f - row * px, 1.0f - (col + 1) * px);
                    } else if (direction.equals(Direction.WEST)) {
                        if (col != 0 && pixelGrid[row][col - 1] != -1) continue; // cull face
                        emitter.square(direction, (1.0f - px)/2, 1.0f - (row + 1) * px, (1.0f + px)/2, 1.0f - row * px, col * px);
                    } else if (direction.equals(Direction.UP)) {
                        if (row != 0 && pixelGrid[row - 1][col] != -1) continue; // cull face
                        emitter.square(direction, col * px, (1.0f - px)/2, (col + 1) * px, (1.0f + px)/2, row * px);
                    } else if (direction.equals(Direction.DOWN)) {
                        if (row != (rowSize - 1) && pixelGrid[row + 1][col] != -1) continue; // cull face
                        emitter.square(direction, col * px, (1.0f - px)/2, (col + 1) * px, (1.0f + px)/2, 1.0f - (row + 1) * px);
                    }

                    emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);

                    emitter.color((255 << 24) | color, (255 << 24) | color, (255 << 24) | color, (255 << 24) | color);

                    emitter.emit();
                }

            }
        }

        mesh = builder.build();

        mesh.outputTo(context.getEmitter());
    }

    HashMap<String, Boolean> markCache = new HashMap<>();

    // This runs when an item needs a model
    private void markCustom(String itemID) {

        if (markCache.containsKey(itemID.toLowerCase())) return;

        String configDir = String.valueOf(FabricLoader.getInstance().getConfigDir());
        String itemsJSONPath = configDir + "/infinicraft/items.json";
        JsonArray items = JsonHandler.readArray(itemsJSONPath, "mark custom");
        if (items == null) return;

        // Iterate through each item
        for (int i = 0; i < items.size(); i++) {

            JsonObject item = items.get(i).getAsJsonObject();

            String itemName = item.get("item").getAsString();
            JsonElement isCustom = item.get("custom");

            if (itemName.equals(itemID) && (isCustom == null || !isCustom.getAsBoolean())) {
                item.addProperty("custom", true);

                System.out.println("Marked " + itemID + " as custom");

                // Update the JSON file
                JsonHandler.writeArray(itemsJSONPath, items, "mark custom");

                markCache.put(itemID.toLowerCase(), true);
                return;
            }
        }
    }
}
