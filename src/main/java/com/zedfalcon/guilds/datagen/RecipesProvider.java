package com.zedfalcon.guilds.datagen;

import com.zedfalcon.guilds.Guilds;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, Guilds.CLAIM_POINT_BLOCK, 1)
                .pattern("eee")
                .pattern("eee")
                .pattern("eee")
                .input('e', Items.GOLD_BLOCK)
                .criterion("get_stuff", InventoryChangedCriterion.Conditions.items(Items.GOLD_BLOCK))
                .offerTo(exporter);
    }
}