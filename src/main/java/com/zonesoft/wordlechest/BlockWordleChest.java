package com.zonesoft.wordlechest;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkHooks;

@EventBusSubscriber
public class BlockWordleChest extends AbstractChestBlock<TEWordleChest> implements IWaterLoggable {
	public static final DirectionProperty FACING = HorizontalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

	@SubscribeEvent
	public static void onBiomeLoading(BiomeLoadingEvent event) {
		event.getGeneration().getFeatures(Decoration.TOP_LAYER_MODIFICATION).add(() -> WordleChest.FLOWER);
	}

	public BlockWordleChest() {
		super(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD), () -> {
			return WordleChest.wordle_chest_tileentity.get();
		});
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED,
				Boolean.valueOf(false)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> combine(BlockState p_225536_1_,
			World p_225536_2_, BlockPos p_225536_3_, boolean p_225536_4_) {
		return TileEntityMerger.ICallback::acceptNone;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
			ISelectionContext p_220053_4_) {
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState p_149645_1_) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
		FluidState fluidstate = p_196258_1_.getLevel().getFluidState(p_196258_1_.getClickedPos());
		return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite())
				.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockRayTraceResult result) {
		TileEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof TEWordleChest) {
			BlockPos blockpos = pos.above();
			if (world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				return ActionResultType.sidedSuccess(world.isClientSide);
			} else if (world.isClientSide) {
				return ActionResultType.SUCCESS;
			} else {
				NetworkHooks.openGui((ServerPlayerEntity) player, (TEWordleChest) tileentity,
						(PacketBuffer packerBuffer) -> {
							packerBuffer.writeBlockPos(tileentity.getBlockPos());
						});
				return ActionResultType.CONSUME;
			}
		} else {
			return ActionResultType.sidedSuccess(world.isClientSide);
		}
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
		return new TEWordleChest();
	}

	@Override
	public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
		return p_185499_1_.setValue(FACING, p_185499_2_.rotate(p_185499_1_.getValue(FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
		return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(FACING, WATERLOGGED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState p_204507_1_) {
		return p_204507_1_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_204507_1_);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState state2, boolean flag) {
		if (!state.is(state2.getBlock())) {
			TileEntity tileentity = world.getBlockEntity(pos);
			if (tileentity instanceof IInventory) {
				InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
			}
			super.onRemove(state, world, pos, state2, flag);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_,
			IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
		if (p_196271_1_.getValue(WATERLOGGED)) {
			p_196271_4_.getLiquidTicks().scheduleTick(p_196271_5_, Fluids.WATER,
					Fluids.WATER.getTickDelay(p_196271_4_));
		}
		return super.updateShape(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
	}

	@Override
	public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_,
			PathType p_196266_4_) {
		return false;
	}

}
