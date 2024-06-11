package net.spiralio.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.spiralio.blocks.screen.InfinicrafterScreenHandler;

public class InfinicrafterScreen extends HandledScreen<InfinicrafterScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("infinicraft", "textures/gui/infinicrafter.png");

    public InfinicrafterScreen(InfinicrafterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    private int shine = 0;
    private int maxShine = 22;

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int slownessModifier = 3;
        int shineSize = 5;
        int realShine = shine / slownessModifier;

//        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
//        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth)/2;
        int y = (height - backgroundHeight)/2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.isCrafting()) {
            context.drawTexture(TEXTURE, x + 93, y + 35, 176, 0, 22, 16);

            context.drawTexture(TEXTURE, x + 93 + Math.max(0, realShine - shineSize), y + 35, 176 + Math.max(0, realShine - shineSize), 16, Math.min(realShine, shineSize), 16);

//            System.out.println(shine);

            shine++;
            if (shine > ((maxShine + shineSize) * slownessModifier)) shine = 0;
        }

//        renderProgressArrow(context, x, y); See 26
    }

    private void renderArrow(DrawContext context, int x, int y) {
        // 26:15
        // Use draw texture w/ custom UV
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
