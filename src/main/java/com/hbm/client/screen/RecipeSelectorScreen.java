package com.hbm.client.screen;

import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
import com.hbm.inventory.recipes.PUREXRecipes;
import com.hbm.lib.RefStrings;
import com.hbm.network.AssemblyRecipePacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 1.7.10 {@code GUIScreenRecipeSelector} for assembly machine, chemical plant, and PUREX.
 */
public class RecipeSelectorScreen extends Screen {
    public interface Catalog {
        List<GenericMachineRecipe> visible(String pool);

        List<GenericMachineRecipe> visible(String pool, String query);

        GenericMachineRecipe byName(String name);
    }

    public static final Catalog ASSEMBLY = new Catalog() {
        @Override
        public List<GenericMachineRecipe> visible(String pool) {
            return AssemblyMachineRecipes.visible(pool);
        }

        @Override
        public List<GenericMachineRecipe> visible(String pool, String query) {
            return AssemblyMachineRecipes.visible(pool, query);
        }

        @Override
        public GenericMachineRecipe byName(String name) {
            return AssemblyMachineRecipes.byName(name);
        }
    };

    public static final Catalog CHEMPLANT = new Catalog() {
        @Override
        public List<GenericMachineRecipe> visible(String pool) {
            return ChemicalPlantRecipes.visible(pool);
        }

        @Override
        public List<GenericMachineRecipe> visible(String pool, String query) {
            return ChemicalPlantRecipes.visible(pool, query);
        }

        @Override
        public GenericMachineRecipe byName(String name) {
            return ChemicalPlantRecipes.byName(name);
        }
    };

    public static final Catalog PUREX = new Catalog() {
        @Override
        public List<GenericMachineRecipe> visible(String pool) {
            return PUREXRecipes.visible(pool);
        }

        @Override
        public List<GenericMachineRecipe> visible(String pool, String query) {
            return PUREXRecipes.visible(pool, query);
        }

        @Override
        public GenericMachineRecipe byName(String name) {
            return PUREXRecipes.byName(name);
        }
    };

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_recipe_selector.png");
    private static final String NULL_SELECTION = "null";

    private final int imageWidth = 176;
    private final int imageHeight = 132;
    private final BlockPos pos;
    private final int index;
    private final String installedPool;
    private final Screen parent;
    private final Catalog catalog;
    private int leftPos;
    private int topPos;
    private EditBox search;
    private List<GenericMachineRecipe> recipes = List.of();
    private String selection;
    private int pageIndex;
    private int size;

    public RecipeSelectorScreen(BlockPos pos, int index, String selection, String installedPool, Screen parent) {
        this(pos, index, selection, installedPool, parent, ASSEMBLY);
    }

    public RecipeSelectorScreen(BlockPos pos, int index, String selection, String installedPool, Screen parent,
                                Catalog catalog) {
        super(Component.translatable("gui.recipe.setRecipe"));
        this.pos = pos;
        this.index = index;
        this.selection = selection == null || selection.isEmpty() ? NULL_SELECTION : selection;
        this.installedPool = installedPool;
        this.parent = parent;
        this.catalog = catalog;
        regenerate();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.search = new EditBox(this.font, leftPos + 28, topPos + 111, 102, 12, Component.empty());
        this.search.setBordered(false);
        this.search.setMaxLength(32);
        this.search.setResponder(this::search);
        this.addWidget(this.search);
        this.setInitialFocus(this.search);
    }

    private void regenerate() {
        this.recipes = catalog.visible(installedPool);
        resetPaging();
    }

    private void search(String query) {
        this.recipes = catalog.visible(installedPool, query);
        resetPaging();
    }

    private void resetPaging() {
        this.pageIndex = 0;
        this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 40) / 8.0D));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (this.search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 108, 0, 132, 106, 16);
        }
        if (hover(152, 18, 16, 16, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 18, 176, 0, 16, 16);
        }
        if (hover(152, 36, 16, 16, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 36, 176, 16, 16, 16);
        }
        if (hover(152, 90, 16, 16, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 90, 176, 32, 16, 16);
        }
        if (hover(134, 108, 16, 16, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 134, topPos + 108, 176, 48, 16, 16);
        }
        if (hover(8, 108, 16, 16, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 108, 176, 64, 16, 16);
        }

        for (int i = pageIndex * 8; i < pageIndex * 8 + 40 && i < recipes.size(); i++) {
            int ind = i - pageIndex * 8;
            GenericMachineRecipe recipe = recipes.get(i);
            if (recipe.name().equals(this.selection)) {
                graphics.blit(TEXTURE, leftPos + 7 + 18 * (ind % 8), topPos + 17 + 18 * (ind / 8), 192, 0, 18, 18);
            }
        }
        for (int i = pageIndex * 8; i < pageIndex * 8 + 40 && i < recipes.size(); i++) {
            int ind = i - pageIndex * 8;
            ItemStack icon = GenericRecipeMatch.icon(recipes.get(i));
            if (!icon.isEmpty()) {
                graphics.renderItem(icon, leftPos + 8 + 18 * (ind % 8), topPos + 18 + 18 * (ind / 8));
            }
        }
        GenericMachineRecipe selected = catalog.byName(this.selection);
        if (selected != null) {
            ItemStack icon = GenericRecipeMatch.icon(selected);
            if (!icon.isEmpty()) {
                graphics.renderItem(icon, leftPos + 152, topPos + 72);
            }
        }
        this.search.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (hover(7, 17, 144, 90, mouseX, mouseY)) {
            for (int i = pageIndex * 8; i < pageIndex * 8 + 40 && i < recipes.size(); i++) {
                int ind = i - pageIndex * 8;
                int ix = 7 + 18 * (ind % 8);
                int iy = 17 + 18 * (ind / 8);
                if (hover(ix, iy, 18, 18, mouseX, mouseY)) {
                    graphics.renderComponentTooltip(this.font, GenericRecipeMatch.tooltip(recipes.get(i)), mouseX, mouseY);
                    break;
                }
            }
        }
        if (hover(151, 71, 18, 18, mouseX, mouseY) && selected != null) {
            graphics.renderComponentTooltip(this.font, GenericRecipeMatch.tooltip(selected), mouseX, mouseY);
        }
        if (hover(152, 90, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Close"), mouseX, mouseY);
        }
        if (hover(134, 108, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Clear search"), mouseX, mouseY);
        }
        if (hover(8, 108, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Press ENTER to toggle focus"), mouseX, mouseY);
        }
    }

    private boolean hover(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w
                && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hover(152, 18, 16, 16, (int) mouseX, (int) mouseY)) {
            click();
            if (pageIndex > 0) {
                pageIndex--;
            }
            return true;
        }
        if (hover(152, 36, 16, 16, (int) mouseX, (int) mouseY)) {
            click();
            if (pageIndex < size) {
                pageIndex++;
            }
            return true;
        }
        if (hover(134, 108, 16, 16, (int) mouseX, (int) mouseY)) {
            this.search.setValue("");
            this.search.setFocused(true);
            return true;
        }
        for (int i = pageIndex * 8; i < pageIndex * 8 + 40 && i < recipes.size(); i++) {
            int ind = i - pageIndex * 8;
            int ix = 7 + 18 * (ind % 8);
            int iy = 17 + 18 * (ind / 8);
            if (hover(ix, iy, 18, 18, (int) mouseX, (int) mouseY)) {
                String name = recipes.get(i).name();
                this.selection = name.equals(this.selection) ? NULL_SELECTION : name;
                click();
                return true;
            }
        }
        if (hover(151, 71, 18, 18, (int) mouseX, (int) mouseY) && !NULL_SELECTION.equals(this.selection)) {
            this.selection = NULL_SELECTION;
            click();
            return true;
        }
        if (hover(152, 90, 16, 16, (int) mouseX, (int) mouseY)) {
            closeToParent();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && pageIndex > 0) {
            pageIndex--;
            return true;
        }
        if (delta < 0 && pageIndex < size) {
            pageIndex++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.search.setFocused(!this.search.isFocused());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            pageIndex = Math.max(0, pageIndex - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            pageIndex = Math.min(size, pageIndex + 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            pageIndex = Math.max(0, pageIndex - 5);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            pageIndex = Math.min(size, pageIndex + 5);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            pageIndex = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            pageIndex = size;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || (this.minecraft != null && keyCode == this.minecraft.options.keyInventory.getKey().getValue())) {
            closeToParent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        ModMessages.CHANNEL.sendToServer(new AssemblyRecipePacket(pos, index, selection));
        super.removed();
    }

    private void closeToParent() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void click() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.pageIndex = Mth.clamp(this.pageIndex, 0, this.size);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
