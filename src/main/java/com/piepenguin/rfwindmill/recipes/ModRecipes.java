package com.piepenguin.rfwindmill.recipes;


import com.google.common.base.Preconditions;
import com.piepenguin.rfwindmill.blocks.ModBlocks;
import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.lib.Constants;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModRecipes {

    public static void init() {
        registerCommonRecipes();
        if (Loader.isModLoaded(Constants.THERMAL_EXPANSION_MOD_ID)) {
            registerThermalExpansionRecipes();
        } else {
            registerVanillaRecipes();
        }
    }

    public static void registerCommonRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.rotor1, "i  ", "iii", "  i", 'i', "ingotIron"));
    }

    public static void registerVanillaRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock1, " x ", "ibi", " r ", 'x', ModItems.rotor1, 'i', "ingotIron", 'r', "dustRedstone"));
    }

    public static void registerThermalExpansionRecipes() {
        ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
        ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));
        GameRegistry.addRecipe(new ShapedOreRecipe(ModBlocks.windmillBlock1, " x ", "gmg", " c ", 'x', ModItems.rotor1, 'g', "gearTin", 'm', machineFrameBasic, 'c', powerCoilSilver));
    }
}