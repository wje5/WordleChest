package com.zonesoft.wordlechest;

import java.util.Random;

import com.zonesoft.wordlechest.TEWordleChest.Answer;
import com.zonesoft.wordlechest.TEWordleChest.WordleData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.loot.LootTables;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("wordlechest")
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class WordleChest {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "wordlechest");
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "wordlechest");
	public static final DeferredRegister<TileEntityType<?>> TILEENTITIES = DeferredRegister
			.create(ForgeRegistries.TILE_ENTITIES, "wordlechest");
	public static final DeferredRegister<ContainerType<?>> CONTAINER = DeferredRegister
			.create(ForgeRegistries.CONTAINERS, "wordlechest");

	public static final RegistryObject<Block> wordle_chest = BLOCKS.register("wordle_chest", BlockWordleChest::new);
	public static final RegistryObject<Item> wordle_chest_item = ITEMS.register("wordle_chest",
			() -> new BlockItem(wordle_chest.get(), new Item.Properties().tab(ItemGroup.TAB_DECORATIONS).stacksTo(1)
					.setISTER(() -> () -> new ITESRWordleChest(TEWordleChest::new))));
	public static final RegistryObject<TileEntityType<TEWordleChest>> wordle_chest_tileentity = TILEENTITIES.register(
			"wordle_chest_tileentity",
			() -> TileEntityType.Builder.of(TEWordleChest::new, wordle_chest.get()).build(null));
	public static final RegistryObject<ContainerType<ContainerWordleChest>> wordle_chest_container = CONTAINER.register(
			"wordle_chest_container",
			() -> IForgeContainerType
					.create((int id, PlayerInventory inv, PacketBuffer data) -> new ContainerWordleChest(id,
							data.readBlockPos(), inv, new WordleData(), new Answer())));

	public static SimpleChannel CHANNEL;
	public static ConfiguredFeature<?, ?> FLOWER;

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("wordlechest:first_networking"),
					() -> "1.0", (version) -> version.equals("1.0"), (version) -> version.equals("1.0"));
			CHANNEL.messageBuilder(MessageMordleGuess.class, 0).encoder(MessageMordleGuess::toBytes)
					.decoder(MessageMordleGuess::new).consumer(MessageMordleGuess::handler).add();
			FLOWER = Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, "flower",
					Feature.RANDOM_PATCH.configured(new BlockClusterFeatureConfig.Builder(
							new WeightedBlockStateProvider().add(wordle_chest.get().defaultBlockState(), 1),
							new SimpleBlockPlacer() {
								@Override
								public void place(IWorld world, BlockPos pos, BlockState state, Random rand) {
									if (!world.getBlockState(pos.below()).isCollisionShapeFullBlock(world, pos)) {
										return;
									}
									super.place(world, pos, state, rand);
									LockableLootTileEntity.setLootTable(world, rand, pos, LootTables.WOODLAND_MANSION);
								}
							}).tries(1).build()))
					.decorated(Placement.TOP_SOLID_HEIGHTMAP.configured(new NoPlacementConfig()));
		});
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ScreenManager.register(wordle_chest_container.get(), GuiContainerWordleChest::new);
			ClientRegistry.bindTileEntityRenderer(wordle_chest_tileentity.get(), e -> new TESRWordleChest(e));
		});
	}

	public WordleChest() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
		TILEENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
		CONTAINER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
