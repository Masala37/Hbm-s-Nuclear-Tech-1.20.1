package com.hbm.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class HbmJeiCategory<T> implements IRecipeCategory<T> {
    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final BiConsumer<IRecipeLayoutBuilder, T> slots;
    private final Function<T, List<Component>> extra;

    HbmJeiCategory(IGuiHelper guiHelper, RecipeType<T> type, String titleKey, ItemStack icon,
                   int width, int height, BiConsumer<IRecipeLayoutBuilder, T> slots,
                   Function<T, List<Component>> extra) {
        this.type = type;
        this.title = Component.translatable(titleKey);
        this.background = guiHelper.createBlankDrawable(width, height);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
        this.slots = slots;
        this.extra = extra == null ? recipe -> List.of() : extra;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        slots.accept(builder, recipe);
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        List<Component> lines = extra.apply(recipe);
        int y = background.getHeight() - 9 * lines.size() - 1;
        var font = Minecraft.getInstance().font;
        for (Component line : lines) {
            graphics.drawString(font, line, 2, y, 0xFF404040, false);
            y += 9;
        }
    }
}
