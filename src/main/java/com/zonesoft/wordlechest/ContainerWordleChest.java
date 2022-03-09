package com.zonesoft.wordlechest;

import com.zonesoft.wordlechest.TEWordleChest.Answer;
import com.zonesoft.wordlechest.TEWordleChest.WordleData;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerWordleChest extends Container {
	private TEWordleChest tileentity;
	private WordleData data;
	private Answer answer;

	public ContainerWordleChest(int id, BlockPos pos, PlayerInventory playerInv, WordleData data, Answer answer) {
		super(WordleChest.wordle_chest_container.get(), id);
		tileentity = getTileEntity(pos);
		this.data = data;
		this.answer = answer;
		addDataSlots(data);
		addDataSlots(answer);
		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(tileentity, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 - 18));
			}
		}
		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 161 - 18));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private TEWordleChest getTileEntity(BlockPos pos) {
		return (TEWordleChest) Minecraft.getInstance().level.getBlockEntity(pos);
	}

	public ContainerWordleChest(int id, TEWordleChest tileentity, PlayerInventory playerInv, WordleData data,
			Answer answer) {
		super(WordleChest.wordle_chest_container.get(), id);
		this.tileentity = tileentity;
		this.data = data;
		this.answer = answer;
		addDataSlots(data);
		addDataSlots(answer);
		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(tileentity, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 - 18));
			}
		}
		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 143));
		}
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < 27) {
				if (!this.moveItemStackTo(itemstack1, 27, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return itemstack;
	}

	public WordleData getData() {
		return data;
	}

	public Answer getAnswer() {
		return answer;
	}

	public TEWordleChest getTileEntity() {
		return tileentity;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}
}
