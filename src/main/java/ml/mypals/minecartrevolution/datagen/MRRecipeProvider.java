package ml.mypals.minecartrevolution.datagen;

import java.util.concurrent.CompletableFuture;
import ml.mypals.minecartrevolution.registeries.MRModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

public class MRRecipeProvider extends RecipeProvider {

  public MRRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
    super(registries, output);
  }

  @Override
  protected void buildRecipes() {

    ShapedRecipeBuilder.shaped(this.items, RecipeCategory.TOOLS, MRModItems.WRENCH.get())
        .pattern("I")
        .pattern("I")
        .define('I', Items.IRON_INGOT)
        .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
        .save(this.output);
  }

  public static class Runner extends RecipeProvider.Runner {

    public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
      super(output, lookupProvider);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(
        HolderLookup.@NonNull Provider provider, @NonNull RecipeOutput output) {
      return new MRRecipeProvider(provider, output);
    }

    @Override
    public @NonNull String getName() {
      return "MinecartRevolution Recipes";
    }
  }
}
