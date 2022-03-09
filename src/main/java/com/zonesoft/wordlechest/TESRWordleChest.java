package com.zonesoft.wordlechest;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.ChestType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public class TESRWordleChest extends ChestTileEntityRenderer<TEWordleChest> {
	public static ResourceLocation TEXTURE = new ResourceLocation("wordlechest:block/wordle_chest");

	public TESRWordleChest(TileEntityRendererDispatcher p_i226008_1_) {
		super(p_i226008_1_);

	}

	@Override
	public void render(TEWordleChest p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_,
			IRenderTypeBuffer p_225616_4_, int p_225616_5_, int p_225616_6_) {
		super.render(p_225616_1_, p_225616_2_, p_225616_3_, p_225616_4_, p_225616_5_, p_225616_6_);
	}

	@Override
	protected RenderMaterial getMaterial(TEWordleChest tileEntity, ChestType chestType) {
		return new RenderMaterial(Atlases.CHEST_SHEET, TEXTURE);
	}

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap().location().equals(Atlases.CHEST_SHEET)) {
			return;
		}
		event.addSprite(TEXTURE);
	}
}