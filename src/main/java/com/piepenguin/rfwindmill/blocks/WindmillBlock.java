package com.piepenguin.rfwindmill.blocks;

import buildcraft.api.tools.IToolWrench;
import com.piepenguin.rfwindmill.lib.Constants;
import com.piepenguin.rfwindmill.lib.EnergyStorage;
import com.piepenguin.rfwindmill.lib.Lang;
import com.piepenguin.rfwindmill.lib.Util;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class WindmillBlock extends Block implements ITileEntityProvider {

    protected final int maximumEnergyGeneration;
    protected final int maximumEnergyTransfer;
    protected final int capacity;

    private String name;
    private IIcon[] icons = new IIcon[6];

    public WindmillBlock(String pName, int pMaximumEnergyGeneration, int pCapacity) {
        super(Material.rock);
        setHardness(3.5f);
        setStepSound(Block.soundTypeMetal);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
        maximumEnergyTransfer = pMaximumEnergyGeneration * 8;
        capacity = pCapacity;
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(this, name);
    }

    @Override
    public void registerBlockIcons(IIconRegister pIconRegister) {
        icons[0] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[1] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[2] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Front");
        icons[3] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Front");
        icons[4] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
        icons[5] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        switch(pSide) {
            case 0:
                return icons[0];
            case 1:
                return icons[1];
            case 2:
                return icons[(pMeta == 0 || pMeta == 2) ? 2 : 4];
            case 3:
                return icons[(pMeta == 0 || pMeta == 2) ? 2 : 4];
            case 4:
                return icons[(pMeta == 1 || pMeta == 3) ? 2 : 4];
            case 5:
                return icons[(pMeta == 1 || pMeta == 3) ? 2 : 4];
            default:
                return icons[0];
        }
    }

    @Override
    public TileEntity createNewTileEntity(World pWorld, int pMeta) {
        return new TileEntityWindmillBlock(maximumEnergyGeneration, maximumEnergyTransfer, capacity);
    }

    @Override
    public boolean hasTileEntity(int pMetadata) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World pWorld, int pX, int pY, int pZ, EntityLivingBase pEntity, ItemStack pItemStack) {
        int direction = MathHelper.floor_double((double) (pEntity.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;
        pWorld.setBlockMetadataWithNotify(pX, pY, pZ, direction, 2);
        if(pItemStack.stackTagCompound != null) {
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
            entity.setEnergyStored(pItemStack.stackTagCompound.getInteger(EnergyStorage.NBT_ENERGY));
        }
        super.onBlockPlacedBy(pWorld, pX, pY, pZ, pEntity, pItemStack);
    }

    @Override
    public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pDx, float pDy, float pDz) {
        if(!pWorld.isRemote) {
            if(pPlayer.isSneaking()) {
                // Dismantle block if player has a wrench
                if(Util.hasWrench(pPlayer, pX, pY, pZ)) {
                    dismantle(pWorld, pX, pY, pZ);
                    return true;
                }
                else {
                    // Print energy information otherwise
                    printChatInfo(pWorld, pX, pY, pZ, pPlayer);
                    return true;
                }
            }
            else {
                if(Util.hasWrench(pPlayer, pX, pY, pZ)) {
                    // Orient the block to face the player
                    int direction = MathHelper.floor_double((double) (pPlayer.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;
                    pWorld.setBlockMetadataWithNotify(pX, pY, pZ, direction, 2);
                }
            }
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World pWorld, int pX, int pY, int pZ, int pSide, EntityPlayer pPlayer) {
        if(!pWorld.isRemote) {
            dismantle(pWorld, pX, pY, pZ);
        }
    }

    private void dismantle(World pWorld, int pX, int pY, int pZ) {
        ItemStack itemStack = new ItemStack(this);

        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
        int energy = entity.getEnergyStored();
        if(energy > 0) {
            if(itemStack.getTagCompound() == null) {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            itemStack.getTagCompound().setInteger(EnergyStorage.NBT_ENERGY, energy);
        }
        pWorld.setBlockToAir(pX, pY, pZ);
        EntityItem entityItem = new EntityItem(pWorld, pX + 0.5, pY + 0.5, pZ + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        pWorld.spawnEntityInWorld(entityItem);
    }

    private void printChatInfo(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer) {
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
        String msg = String.format("%s: %d/%d RF",
                Lang.localise("energy.stored"),
                entity.getEnergyStored(),
                entity.getMaxEnergyStored());
        pPlayer.addChatMessage(new ChatComponentText(msg));
    }
}
