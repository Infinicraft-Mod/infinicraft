package net.spiralio.models;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.spiralio.Infinicraft;
import net.spiralio.util.JsonHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.render.model.json.Transformation;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class InfiniteModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final SpriteIdentifier SPRITE_ID = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("infinicraft:block/white")
    );

    private Sprite sprite = null;
    private Sprite stone = null;

    private HashMap<String, Mesh> meshCache = new HashMap<>();
    private HashMap<String, Mesh> temporaryMeshCache = new HashMap<>();

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

    private static final ModelTransformation MODEL_TRANSFORMATION;

    static {
        final Transformation TRANSFORM_BLOCK_GUI = makeTransform(0, 0, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
        final Transformation TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 2, 0, 0.5f, 0.5f, 0.5f);
        final Transformation TRANSFORM_BLOCK_FIXED = makeTransform(0, 180.0f, 0, 0, 0, 0, 1.0f, 1.0f, 1.0f);
        final Transformation TRANSFORM_BLOCK_3RD_PERSON = makeTransform(0, 0, 0, 0, 3.0f, 1.0f, 0.55f, 0.55f, 0.55f);
        final Transformation TRANSFORM_BLOCK_1ST_PERSON = makeTransform(0, -90.0f, 25.0f, 1.13f, 3.2f, 1.13f, 0.68f, 0.68f, 0.68f);
        final Transformation TRANSFORM_BLOCK_HEAD = makeTransform(0, 180.0f, 0, 0, 13.0f, 7.0f, 1.0f, 1.0f, 1.0f);

        MODEL_TRANSFORMATION = new ModelTransformation(TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_3RD_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_1ST_PERSON, TRANSFORM_BLOCK_HEAD, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED);
    }

    @Override
    public ModelTransformation getTransformation() {
        return MODEL_TRANSFORMATION;
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

    private final HashMap<String, int[]> storedTextures = new HashMap<>();
    private int tickCount = 0;

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext context) {
        tickCount = (tickCount + 1) % 20;

        int[] pixelGrid = null;
        String thisColor = null;

        String itemIdLower = null;

        if (itemStack.hasNbt()) {
            // If the itemData has an itemData ID
            String itemID = itemStack.getNbt().getString("item");
            thisColor = itemStack.getNbt().getString("color");

            itemIdLower = itemID.toLowerCase(Locale.ROOT);
            if (!itemID.isEmpty()) {
                // If a cached mesh already exists, just render it as-is
                if (meshCache.containsKey(itemIdLower)) {
                    meshCache.get(itemIdLower).outputTo(context.getEmitter());
                    return;
                }

                // Try to get a texture for this itemData ID

                // Look for a texture and set it
                pixelGrid = storedTextures.getOrDefault(itemIdLower, null);

                if (pixelGrid == null && (tickCount % 10 == 0)) {
                    var itemData = JsonHandler.getItemById(itemID);

                    if (itemData != null) {
                        // Find a matching itemData
                        int[] textureElement = itemData.getTexture();

                        // No texture in this object
                        if (textureElement == null) {
                            // Mark as needing a texture (since it has an ID)
                            markCustom(itemID);
                        } else {
                            storedTextures.put(itemIdLower, pixelGrid = textureElement);
                        }
                    }
                }
            }

            if (pixelGrid == null) {
                // Texture NBT
                final NbtList texture = itemStack.getNbt().getList("Texture", 9);

                if (!texture.isEmpty()) {
                    pixelGrid = new int[256];

                    final int width = 16;

                    // Allow for textures of 1-dimensional 256 entries or 2-dimensional 16x16
                    for (int i = 0; i < texture.size(); i++) {
                        NbtElement el = texture.get(i);
                        if (el.getType() == NbtElement.LIST_TYPE) {
                            NbtList row = (NbtList) el;
                            for (int j = 0; j < row.size(); j++) {
                                pixelGrid[(j * width) + i] = row.getInt(j);
                            }
                        } else {
                            pixelGrid[i] = ((NbtInt) el).intValue();
                        }
                    }
                }
            }

            // If a cached temp mesh already exists, just render it as-is
            if (pixelGrid == null && temporaryMeshCache.containsKey(itemIdLower)) {
                temporaryMeshCache.get(itemIdLower).outputTo(context.getEmitter());
                return;
            }
        }

        boolean isTempMesh = false;

        if (pixelGrid == null) {
            pixelGrid = MissingTextures.getColor(thisColor);
            isTempMesh = true;
        }

        Mesh renderedMesh = renderModel(context, pixelGrid);

        if (itemIdLower != null && !itemIdLower.isEmpty()) {
            if (!isTempMesh) {
                temporaryMeshCache.remove(itemIdLower);
                meshCache.put(itemIdLower, renderedMesh);
            } else {
                temporaryMeshCache.put(itemIdLower, renderedMesh);
            }
        }
    }

    private Mesh renderModel(RenderContext context, int[] pixelGrid) {
        final int height = 16;
        final int width = 16;

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        float px = 0.0625f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int color = pixelGrid[(y * width) + x]; // to index the array: multiply y by the stride (width) and add x
                if (color == -1) continue;

                for (Direction direction: Direction.values()) {
                    if (direction.equals(Direction.NORTH)) {
                        emitter.square(direction, 1.0f - (x + 1) * px, 1.0f - (y + 1) * px, 1.0f - x * px, 1.0f - y * px, (15 * px)/2);
                    } else if (direction.equals(Direction.SOUTH)) {
                        emitter.square(direction, x * px, 1.0f - (y + 1) * px, (x + 1) * px, 1.0f - y * px, (1.0f - px)/2);
                    } else if (direction.equals(Direction.EAST)) {
                        if (x != (width - 1) && pixelGrid[(y * width) + (x + 1)] != -1) continue; // cull face
                        emitter.square(direction, (1.0f - px)/2, 1.0f - (y + 1) * px, (1.0f + px)/2, 1.0f - y * px, 1.0f - (x + 1) * px);
                    } else if (direction.equals(Direction.WEST)) {
                        if (x != 0 && pixelGrid[(y * width) + (x - 1)] != -1) continue; // cull face
                        emitter.square(direction, (1.0f - px)/2, 1.0f - (y + 1) * px, (1.0f + px)/2, 1.0f - y * px, x * px);
                    } else if (direction.equals(Direction.UP)) {
                        if (y != 0 && pixelGrid[((y - 1) * width) + x] != -1) continue; // cull face
                        emitter.square(direction, x * px, (1.0f - px)/2, (x + 1) * px, (1.0f + px)/2, y * px);
                    } else if (direction.equals(Direction.DOWN)) {
                        if (y != (height - 1) && pixelGrid[((y + 1) * width) + x] != -1) continue; // cull face
                        emitter.square(direction, x * px, (1.0f - px)/2, (x + 1) * px, (1.0f + px)/2, 1.0f - (y + 1) * px);
                    }

                    emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);

                    emitter.color((255 << 24) | color, (255 << 24) | color, (255 << 24) | color, (255 << 24) | color);

                    emitter.emit();
                }

            }
        }

        Mesh mesh = builder.build();

        mesh.outputTo(context.getEmitter());

        return mesh;
    }

    private final HashMap<String, Boolean> markCache = new HashMap<>();

    // This runs when an itemData needs a model
    private void markCustom(String itemID) {
        String itemIdLower = itemID.toLowerCase(Locale.ROOT);
        if (markCache.containsKey(itemIdLower)) return;

        var itemData = JsonHandler.getItemById(itemID);

        if (itemData != null && !itemData.isCustom()) {
            itemData.setCustom(true);

            JsonHandler.saveItem(itemData);

            Infinicraft.LOGGER.info("Marked {} as custom", itemID);

            markCache.put(itemIdLower, true);
        }
    }
}
