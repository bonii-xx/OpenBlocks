package openblocks.common.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import openblocks.OpenBlocks;
import openblocks.common.Stencil;
import openblocks.common.tileentity.TileEntityCanvas;

public class BlockCanvas extends OpenBlock {

	private int layer = 0;
	private int renderSide = 0;
	public IIcon baseIcon;
	public IIcon wallpaper;

	public BlockCanvas() {
		this(Material.rock);
	}

	public BlockCanvas(Material material) {
		super(material);
	}

	@Override
	public void registerBlockIcons(IIconRegister registry) {
		blockIcon = baseIcon = registry.registerIcon("openblocks:canvas");
		wallpaper = registry.registerIcon("openblocks:wallpaper");
		for (Stencil stencil : Stencil.values())
			stencil.registerBlockIcons(registry);
	}

	@Override
	protected void getCustomTileEntityDrops(TileEntity te, List<ItemStack> result, int fortune) {
		if (!(te instanceof TileEntityCanvas)) return;

		TileEntityCanvas tile = (TileEntityCanvas)te;
		if (tile.paintedBlock.containsValidBlock()) {
			Block paintedBlock = tile.paintedBlock.getValue();
			int paintedBlockMeta = tile.paintedBlockMeta.get();

			final Random rand = te.getWorldObj().rand;

			int count = paintedBlock.quantityDropped(paintedBlockMeta, fortune, rand);
			int damageDropped = paintedBlock.damageDropped(paintedBlockMeta);

			for (int i = 0; i < count; i++) {
				Item item = paintedBlock.getItemDropped(paintedBlockMeta, rand, fortune);
				if (item != null) result.add(new ItemStack(item, 1, damageDropped));

			}
		} else {
			result.add(new ItemStack(this));
		}
	}

	@Override
	protected boolean hasNormalDrops() {
		return false;
	}

	@Override
	public boolean shouldRenderBlock() {
		return true;
	}

	public void setLayerForRender(int layer) {
		this.layer = layer;
	}

	public void setSideForRender(int side) {
		this.renderSide = side;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return side == renderSide && super.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		TileEntityCanvas tile = getTileEntity(world, x, y, z, TileEntityCanvas.class);
		return tile != null? tile.getColorForRender(renderSide, layer) : 0xFFFFFFFF;
	}

	@Override
	public IIcon getUnrotatedTexture(ForgeDirection direction, IBlockAccess world, int x, int y, int z) {
		TileEntityCanvas tile = getTileEntity(world, x, y, z, TileEntityCanvas.class);
		if (tile != null) { return tile.getTextureForRender(renderSide, layer); }
		return super.getUnrotatedTexture(direction, world, x, y, z);

	}

	public static void replaceBlock(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if (block.getMaterial() == Material.glass) {
			world.setBlock(x, y, z, OpenBlocks.Blocks.canvasGlass);
		} else {
			world.setBlock(x, y, z, OpenBlocks.Blocks.canvas);
		}
		TileEntityCanvas tile = (TileEntityCanvas)world.getTileEntity(x, y, z);
		tile.paintedBlock.setValue(block);
		tile.paintedBlockMeta.set(meta);
	}
}
