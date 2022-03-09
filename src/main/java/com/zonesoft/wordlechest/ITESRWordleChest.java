package com.zonesoft.wordlechest;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class ITESRWordleChest extends ItemStackTileEntityRenderer {
	private Supplier<TEWordleChest> te;

	public ITESRWordleChest(Supplier<TEWordleChest> te) {
		this.te = te;
	}

	@Override
	public void renderByItem(ItemStack stack, TransformType type, MatrixStack matrixStack, IRenderTypeBuffer buffer,
			int combinedLight, int combinedOverlay) {
		TileEntityRendererDispatcher.instance.renderItem(this.te.get(), matrixStack, buffer, combinedLight,
				combinedOverlay);
	}
}
