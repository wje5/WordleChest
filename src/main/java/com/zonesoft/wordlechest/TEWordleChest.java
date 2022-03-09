package com.zonesoft.wordlechest;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class TEWordleChest extends LockableLootTileEntity implements INamedContainerProvider, IChestLid {
	private String word = "";
	private WordleData data = new WordleData();
	private Answer answerData = new Answer();
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	public static String[][] words = new String[][] { {}, {} };
	static {
		try {
			InputStream stream = TEWordleChest.class.getResourceAsStream("/assets/wordlechest/words/words.json");
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer, StandardCharsets.UTF_8.name());
			String str = writer.toString();
			words = new Gson().fromJson(str, words.getClass());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TEWordleChest() {
		super(WordleChest.wordle_chest_tileentity.get());
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new ContainerWordleChest(id, this, inventory, data, answerData);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("container.wordlechest");
	}

	public String getWord() {
		if (word.isEmpty()) {
			word = words[0][new Random().nextInt(words[0].length)].toUpperCase();
		}
		return word;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		word = nbt.getString("word");
		data = new WordleData();
		if (nbt.contains("data")) {
			int[] array = nbt.getIntArray("data");
			data.setArray(array);
		}
		answerData = new Answer();
		if (data.getTotalLine() == 6 && !isUnlocked()) {
			answerData.setAnswer(word);
		}
		if (!this.tryLoadLootTable(nbt)) {
			ItemStackHelper.loadAllItems(nbt, this.items);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		nbt.putString("word", word);
		nbt.putIntArray("data", data.getArray());
		if (!this.trySaveLootTable(nbt)) {
			ItemStackHelper.saveAllItems(nbt, this.items);
		}
		return nbt;
	}

	public void guess(String guess) {
		if (data.getTotalLine() < 6 && !isUnlocked() && guess.length() == 5 && guess.matches("^[A-Z]+$")) {
			String lower = guess.toLowerCase();
			boolean flag = false;
			for (String s : words[0]) {
				if (s.equals(lower)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				for (String s : words[1]) {
					if (s.equals(lower)) {
						flag = true;
						break;
					}
				}
			}
			if (!flag) {
				return;
			}
			GuessState state = new GuessState(new char[5], new CharState[5]);
			for (int i = 0; i < 5; i++) {
				char c = guess.charAt(i);
				state.guess[i] = c;
				if (getWord().charAt(i) == c) {
					state.state[i] = CharState.GREEN;
					continue;
				}
				for (int j = 0; j < 5; j++) {
					if (getWord().charAt(j) == c) {
						state.state[i] = CharState.YELLOW;
						break;
					}
				}
				if (state.state[i] == null) {
					state.state[i] = CharState.BLACK;
				}
			}
			data.addState(state);
			if (data.getTotalLine() == 6 && !isUnlocked()) {
				answerData.setAnswer(word);
			}
			setChanged();
		}
	}

	public boolean isUnlocked() {
		if (word.isEmpty() || data.getTotalLine() == 0) {
			return false;
		}
		GuessState state = data.getLine(data.getTotalLine() - 1);
		return state.state[0] == CharState.GREEN && state.state[1] == CharState.GREEN
				&& state.state[2] == CharState.GREEN && state.state[3] == CharState.GREEN
				&& state.state[4] == CharState.GREEN;

	}

	@Override
	public float getOpenNess(float p_195480_1_) {
		return 0;
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;

	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.wordle_chest");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory inv) {
		return new ContainerWordleChest(id, this, inv, data, answerData);
	}

	@Override
	public void unpackLootTable(PlayerEntity player) {
		if (!isUnlocked()) {
			return;
		}
		if (this.lootTable != null && this.level.getServer() != null) {
			LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
			if (player instanceof ServerPlayerEntity) {
				CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayerEntity) player, this.lootTable);
			}

			this.lootTable = null;
			LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) this.level))
					.withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(this.worldPosition))
					.withOptionalRandomSeed(this.lootTableSeed);
			float luck = (player == null ? 0 : player.getLuck()) + 6 - data.getTotalLine();
			lootcontext$builder.withLuck(luck);
			if (player != null) {
				lootcontext$builder.withParameter(LootParameters.THIS_ENTITY, player);
			}

			loottable.fill(this, lootcontext$builder.create(LootParameterSets.CHEST));
		}
	}

	public static class WordleData implements IIntArray {
		private int[] array = new int[30];

		@Override
		public void set(int index, int data) {
			array[index] = data;
		}

		@Override
		public int getCount() {
			return 30;
		}

		@Override
		public int get(int index) {
			return array[index];
		}

		public int[] getArray() {
			return array;
		}

		public void setArray(int[] array) {
			this.array = array;
		}

		private void setState(int index, char c, CharState state) {
			set(index, c * 4 + state.ordinal());
		}

		public void addState(GuessState state) {
			int line = getTotalLine();
			for (int i = 0; i < 5; i++) {
				setState(line * 5 + i, state.guess[i], state.state[i]);
			}
		}

		public char getChar(int index) {
			return (char) (get(index) / 4);
		}

		public CharState getState(int index) {
			return CharState.values()[get(index) % 4];
		}

		public int getTotalLine() {
			for (int i = 6; i > 0; i--) {
				if (get((i - 1) * 5) > 0) {
					return i;
				}
			}
			return 0;
		}

		public GuessState getLine(int line) {
			return new GuessState(
					new char[] { getChar(line * 5), getChar(line * 5 + 1), getChar(line * 5 + 2), getChar(line * 5 + 3),
							getChar(line * 5 + 4) },
					new CharState[] { getState(line * 5), getState(line * 5 + 1), getState(line * 5 + 2),
							getState(line * 5 + 3), getState(line * 5 + 4) });
		}
	}

	public static class Answer implements IIntArray {
		private int[] array = new int[5];

		@Override
		public void set(int index, int data) {
			array[index] = data;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public int get(int index) {
			return array[index];
		}

		public boolean isEmpty() {
			return array[0] == 0;
		}

		public void setAnswer(String s) {
			for (int i = 0; i < 5; i++) {
				set(i, s.charAt(i));
			}
		}

		public String getAnswer() {
			if (isEmpty()) {
				return "";
			}
			String s = "";
			for (int i = 0; i < 5; i++) {
				s += (char) get(i);
			}
			return s;
		}
	}

	public static enum CharState {
		GRAY, BLACK, YELLOW, GREEN;

		public int getColor() {
			return this == GRAY ? 0xFF000000 : 0xFFFFFFFF;
		}
	}

	public static class GuessState {
		public char[] guess = new char[5];
		public CharState[] state = new CharState[5];

		public GuessState(char[] guess, CharState[] state) {
			this.guess = guess;
			this.state = state;
		}
	}
}
