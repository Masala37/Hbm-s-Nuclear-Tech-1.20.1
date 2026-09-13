package com.hbm.client.screen;

import com.hbm.inventory.menu.AnvilMenu;
import com.hbm.inventory.recipes.IngredientRef;
import com.hbm.inventory.recipes.PlayerIngredientStore;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.network.AnvilCraftPacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/processing/gui_anvil.png");

    private final int tier;
    private final List<AnvilConstructionRecipe> origin = new ArrayList<>();
    private final List<AnvilConstructionRecipe> recipes = new ArrayList<>();
    private EditBox search;
    private int index;
    private int size;
    private int selection = -1;
    private int lastSize;

    public AnvilScreen(AnvilMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.tier = menu.tier;
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
        for (AnvilConstructionRecipe recipe : AnvilRecipes.getConstruction()) {
            if (recipe.isTierValid(this.tier)) {
                this.origin.add(recipe);
            }
        }
        regenerate();
    }

    @Override
    protected void init() {
        super.init();
        this.search = new EditBox(this.font, this.leftPos + 10, this.topPos + 111, 84, 12, Component.empty());
        this.search.setBordered(false);
        this.search.setMaxLength(25);
        this.search.setResponder(this::applySearch);
        this.addWidget(this.search);
        this.setInitialFocus(this.search);
    }

    private void regenerate() {
        this.recipes.clear();
        this.recipes.addAll(this.origin);
        resetPaging();
    }

    private void applySearch(String text) {
        String needle = text.toLowerCase(Locale.US);
        this.recipes.clear();
        if (needle.isEmpty()) {
            this.recipes.addAll(this.origin);
        } else {
            for (AnvilConstructionRecipe recipe : this.origin) {
                for (String name : searchNames(recipe)) {
                    if (name.contains(needle)) {
                        this.recipes.add(recipe);
                        break;
                    }
                }
            }
        }
        resetPaging();
    }

    private void resetPaging() {
        this.index = 0;
        this.selection = -1;
        this.size = Math.max(0, (int) Math.ceil((this.recipes.size() - 10) / 2.0D));
    }

    private static List<String> searchNames(AnvilConstructionRecipe recipe) {
        List<String> names = new ArrayList<>();
        for (IngredientRef in : recipe.input) {
            names.add(in.searchName().toLowerCase(Locale.US));
            ItemStack stack = in.resultStack();
            if (!stack.isEmpty()) {
                names.add(stack.getHoverName().getString().toLowerCase(Locale.US));
            }
        }
        for (IngredientRef out : recipe.output) {
            ItemStack stack = out.resultStack();
            if (!stack.isEmpty()) {
                names.add(stack.getHoverName().getString().toLowerCase(Locale.US));
            }
        }
        return names;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int slide = Mth.clamp(this.lastSize - 42, 0, 1000);
        int mul = 1;
        while (slide >= 51 * mul) {
            graphics.blit(TEXTURE, x + 125 + 51 * mul, y + 17, 125, 17, 54, 108);
            mul++;
        }
        graphics.blit(TEXTURE, x + 125 + slide, y + 17, 125, 17, 54, 108);

        if (this.search.isFocused()) {
            graphics.blit(TEXTURE, x + 8, y + 108, 168, 222, 88, 16);
        }
        if (hover(x + 7, y + 71, 9, 36, mouseX, mouseY)) {
            graphics.blit(TEXTURE, x + 7, y + 71, 176, 186, 9, 36);
        }
        if (hover(x + 106, y + 71, 9, 36, mouseX, mouseY)) {
            graphics.blit(TEXTURE, x + 106, y + 71, 185, 186, 9, 36);
        }
        if (hover(x + 52, y + 53, 18, 18, mouseX, mouseY)) {
            graphics.blit(TEXTURE, x + 52, y + 53, 176, 150, 18, 18);
        }

        for (int i = index * 2; i < index * 2 + 10; i++) {
            if (i >= recipes.size()) {
                break;
            }
            int ind = i - index * 2;
            AnvilConstructionRecipe recipe = recipes.get(i);
            ItemStack display = recipe.getDisplay();
            int ix = x + 17 + 18 * (ind / 2);
            int iy = y + 72 + 18 * (ind % 2);
            graphics.renderItem(display, ix, iy);
            graphics.renderItemDecorations(this.font, display, ix, iy);
            graphics.blit(TEXTURE, x + 16 + 18 * (ind / 2), y + 71 + 18 * (ind % 2),
                    18 + 18 * recipe.getOverlay().ordinal(), 222, 18, 18);
            if (selection == i) {
                graphics.blit(TEXTURE, x + 16 + 18 * (ind / 2), y + 71 + 18 * (ind % 2), 0, 222, 18, 18);
            }
        }
        this.search.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = Component.translatable("container.anvil", tier).getString();
        graphics.drawString(this.font, name, 61 - this.font.width(name) / 2, 8, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);

        if (this.selection >= 0 && this.selection < recipes.size()) {
            AnvilConstructionRecipe recipe = recipes.get(this.selection);
            List<String> list = recipeLines(recipe);
            int longest = 0;
            for (String line : list) {
                longest = Math.max(longest, this.font.width(line));
            }
            graphics.pose().pushPose();
            graphics.pose().scale(0.5F, 0.5F, 0.5F);
            int offset = 0;
            for (String line : list) {
                graphics.drawString(this.font, line, 260, 50 + offset, 0xFFFFFF, false);
                offset += 9;
            }
            this.lastSize = (int) (longest * 0.5D);
            graphics.pose().popPose();
        } else {
            this.lastSize = 0;
        }
    }

    private List<String> recipeLines(AnvilConstructionRecipe recipe) {
        List<String> list = new ArrayList<>();
        list.add("§eInputs:");
        for (IngredientRef in : recipe.input) {
            int have = PlayerIngredientStore.countMatching(this.minecraft.player, in);
            String name = in.resultStack().isEmpty() ? in.searchName() : in.resultStack().getHoverName().getString();
            String line = ">" + in.count() + "x " + name;
            if (have < in.count()) {
                line = "§c" + line;
            }
            list.add(line);
        }
        list.add("");
        list.add("§eOutputs:");
        for (IngredientRef out : recipe.output) {
            ItemStack stack = out.resultStack();
            String name = stack.isEmpty() ? out.searchName() : stack.getHoverName().getString();
            String line = ">" + out.count() + "x " + name;
            if (out.chance() != 1.0F) {
                line += " (" + (out.chance() * 100) + "%)";
            }
            list.add(line);
        }
        return list;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.search.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int x = this.leftPos;
        int y = this.topPos;
        if (hover(x + 7, y + 71, 9, 36, mouseX, mouseY)) {
            if (this.index > 0) {
                this.index--;
            }
            return true;
        }
        if (hover(x + 106, y + 71, 9, 36, mouseX, mouseY)) {
            if (this.index < this.size) {
                this.index++;
            }
            return true;
        }
        if (hover(x + 52, y + 53, 18, 18, mouseX, mouseY)) {
            if (this.selection >= 0 && this.selection < this.recipes.size()) {
                AnvilConstructionRecipe recipe = this.recipes.get(this.selection);
                int fullIndex = AnvilRecipes.getConstruction().indexOf(recipe);
                boolean shift = hasShiftDown();
                ModMessages.CHANNEL.sendToServer(new AnvilCraftPacket(fullIndex, shift ? 1 : 0));
            }
            return true;
        }
        for (int i = index * 2; i < index * 2 + 10; i++) {
            if (i >= recipes.size()) {
                break;
            }
            int ind = i - index * 2;
            int ix = x + 16 + 18 * (ind / 2);
            int iy = y + 71 + 18 * (ind % 2);
            if (hover(ix, iy, 18, 18, mouseX, mouseY)) {
                this.selection = this.selection == i ? -1 : i;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && this.index > 0) {
            this.index--;
            return true;
        }
        if (delta < 0 && this.index < this.size) {
            this.index++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.search.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.search.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.search.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private static boolean hover(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static boolean hover(int x, int y, int w, int h, int mouseX, int mouseY) {
        return hover(x, y, w, h, (double) mouseX, mouseY);
    }
}
