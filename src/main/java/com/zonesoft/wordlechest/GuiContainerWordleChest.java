package com.zonesoft.wordlechest;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.zonesoft.wordlechest.TEWordleChest.CharState;
import com.zonesoft.wordlechest.TEWordleChest.GuessState;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiContainerWordleChest extends ContainerScreen<ContainerWordleChest> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(
			"wordlechest:textures/gui/container/wordle_chest.png");
	public static final ResourceLocation TEXTURE_2 = new ResourceLocation(
			"wordlechest:textures/gui/container/wordle_chest_2.png");
	public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(
			"textures/gui/container/generic_54.png");

	public static Font FONT_16 = new Font("16pix", 1269);
	public static Font FONT_32 = new Font("32pix", 1353);
	public static Font FONT_BLACK = new Font("arial_black", 1557);

	private String input = "";
	private int typeTick, flipTick, shakeTick, errorTick, jumpTick, successTick;
	private boolean errorType, quitContainer;

	public GuiContainerWordleChest(ContainerWordleChest container, PlayerInventory inventory, ITextComponent title) {
		super(container, inventory, title);
		this.passEvents = false;
		this.inventoryLabelY = 74;
	}

	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		drawTexture(CONTAINER_BACKGROUND, i, j, 0, 0, 176, 71, 1);
		drawTexture(CONTAINER_BACKGROUND, i, j + 71, 0, 126, 176, 96, 1);
	}

	@Override
	public void tick() {
		super.tick();
		typeTick = Math.max(typeTick - 1, 0);
		if (flipTick == 1) {
			if (isUnlocked()) {
				jumpTick = 25;
				successTick = 50;
			}
		}
		flipTick = Math.max(flipTick - 1, 0);
		shakeTick = Math.max(shakeTick - 1, 0);
		errorTick = Math.max(errorTick - 1, 0);
		jumpTick = Math.max(jumpTick - 1, 0);
		successTick = Math.max(successTick - 1, 0);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		fill(stack, 0, 0, width, height, 0xFFFFFFFF);

		float keyboardX = (width - 121) * 0.5F, keyboardY = height - 50F, displayX = (width - 82.5F) * 0.5F,
				displayY = (height - 150F) * 0.5F;

		for (int i = 0; i < 10; i++) {
			renderButton(keyboardX + i * 12.25F, keyboardY, "QWERTYUIOP".charAt(i));
		}
		for (int i = 0; i < 9; i++) {
			renderButton(keyboardX + 6F + i * 12.25F, keyboardY + 16.5F, "ASDFGHJKL".charAt(i));
		}
		for (int i = 0; i < 7; i++) {
			renderButton(keyboardX + 18.25F + i * 12.25F, keyboardY + 33F, "ZXCVBNM".charAt(i));
		}
		drawTexture(TEXTURE, keyboardX, keyboardY + 33F, 0, 58, 67, 58, 0.25F);
		drawTexture(TEXTURE, keyboardX + 104F, keyboardY + 33F, 67, 58, 68, 58, 0.25F);
		for (int i = 0; i < 6; i++) {
			GuessState state = getGuessState(i);
			for (int j = 0; j < 5; j++) {
				boolean flag = state.state[j] == CharState.GRAY && (j == 4 || state.state[j + 1] == null);
				int flip = flipTick == 0 || i != menu.getData().getTotalLine() - 1 || 4 - flipTick / 10 != j ? 0
						: flipTick % 10;
				flip = 0;
				if (flipTick != 0 && i == menu.getData().getTotalLine() - 1) {
					if (50 - flipTick <= j * 10) {
						flip = 10;
					} else if (4 - flipTick / 10 == j) {
						flip = flipTick % 10;
					}
				}
				float jump = 0;
				if (jumpTick > 0 && i == menu.getData().getTotalLine() - 1 && jumpTick < 20) {
					if (jumpTick >= 12 - j * 3) {
						switch ((3 - (jumpTick - j * 3) % 4)) {
						case 0:
							jump = 0.5F;
							break;
						case 1:
							jump = 6.5F;
							break;
						case 2:
							jump = 7.5F;
							break;
						case 3:
							jump = -0.5F;
							break;
						}
					}
					if (jumpTick < 16 - j * 3) {
						jump *= 0.3F;
					}
				}
				renderBox(displayX + j * 16.75F, displayY + i * 16.75F - jump, state.guess[j], state.state[j], flag,
						flip, i == menu.getData().getTotalLine());
			}
		}
		if (errorTick > 0) {
			drawTexture(TEXTURE, (width - (errorType ? 37.25F : 43.5F)) / 2, (displayY - 16F) / 2 + 4F, 0,
					errorType ? 116 : 166, errorType ? 149 : 174, 50, 0.25F);
		}
		if (successTick > 0) {
			int type = menu.getData().getTotalLine();
			int u = type == 6 ? 87 : 0;
			int v = type == 6 ? 0 : (type - 1) * 50;
			int width = 0;
			switch (type) {
			case 1:
				width = 87;
				break;
			case 2:
				width = 122;
				break;
			case 3:
				width = 117;
				break;
			case 4:
				width = 101;
				break;
			case 5:
			case 6:
				width = 75;
				break;
			}
			drawTexture(TEXTURE_2, (this.width - width * 0.25F) / 2, (displayY - 16F) / 2 + 4F, u, v, width, 50, 0.25F);
		}
		if (!menu.getAnswer().isEmpty()) {
			drawTexture(TEXTURE, (this.width - 23F) / 2, (displayY - 16F) / 2 + 4F, 149, 116, 92, 50, 0.25F);
			String answer = menu.getAnswer().getAnswer();
			float x = (this.width - 23F) / 2 + (23 - getStringWidth(answer, FONT_BLACK) * 0.25F) / 2;
			float y = (displayY - 16F) / 2 + 4F + (12.5F - FONT_BLACK.getLineHeight() * 0.25F) * 0.5F;
			for (char c : answer.toCharArray()) {
				x += renderChar(x, y, c, 0xFFFFFFFF, FONT_BLACK, 0.25F);
			}
		}
		if (isUnlocked() && successTick == 0 && flipTick == 0 && !quitContainer) {
			super.render(stack, mouseX, mouseY, partialTicks);
		}
		this.renderTooltip(stack, mouseX, mouseY);
	}

	public void renderButton(float x, float y, char c) {
		CharState state = getCharState(c);
		drawTexture(TEXTURE, x, y, 43 * state.ordinal(), 0, 43, 58, 0.25F);
		renderChar(x + 5.375F - getCharWidth(c, FONT_16) * 0.125F, y + 7.25F - FONT_16.getLineHeight() * 0.125F, c,
				state.getColor(), FONT_16, 0.25F);
	}

	public void renderBox(float x, float y, char c, CharState state, boolean type, int flip, boolean shake) {
		float offset = type ? typeTick / 10F : 0;
		float flipOffset = flip >= 5 ? 7.75F - 7.75F * (flip - 5) / 5 : 7.75F * flip / 5;
		float shakeOffset = shake ? shakeTick % 4 == 1 ? -1 : shakeTick % 4 == 3 ? 1 : 0 : 0;
		float flipScale = flip >= 5 ? (flip - 5) / 5F : 1 - flip / 5F;
		float scale = type ? 1 + (4F / 62F / 5 * typeTick) : 1;
		if (state == null) {
			drawBorder(x + shakeOffset, y, 62 * 0.25F, 62 * 0.25F, 0.5F, 0xFFD8D8DE);
			return;
		}
		if (flip > 5) {
			state = CharState.GRAY;
		}
		if (state == CharState.GRAY) {
			drawBorder(x - offset + shakeOffset, y - offset + flipOffset, 62 * 0.25F * scale,
					62 * 0.25F * scale * flipScale, 0.5F, 0xFF878A8C);
		} else if (state == CharState.BLACK) {
			drawRect(x + shakeOffset, y + flipOffset, 15.5F * scale, 15.5F * scale * flipScale, 0xFF787C7E);
		} else if (state == CharState.YELLOW) {
			drawRect(x + shakeOffset, y + flipOffset, 15.5F * scale, 15.5F * scale * flipScale, 0xFFC9B458);
		} else if (state == CharState.GREEN) {
			drawRect(x + shakeOffset, y + flipOffset, 15.5F * scale, 15.5F * scale * flipScale, 0xFF6AAA64);
		}
		renderChar(x + 7.75F - getCharWidth(c, FONT_32) * 0.125F * scale + shakeOffset,
				y + 7.75F - FONT_32.getLineHeight() * 0.125F * scale * flipScale, c, state.getColor(), FONT_32,
				0.25F * scale, 0.25F * scale * flipScale);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		float keyboardX = (width - 121) * 0.5F, keyboardY = height - 50F;
		double x = mouseX - keyboardX;
		int index = -1;
		if (mouseY >= keyboardY && mouseY <= keyboardY + 14.5F) {
			if (x % 12.25F <= 10.75F && x / 12.25F >= 0 && x / 12.25F < 10) {
				index = (int) Math.floor(x / 12.25F);
			}
		} else if (mouseY >= keyboardY + 16.5F && mouseY <= keyboardY + 31F) {
			x -= 6F;
			if (x % 12.25F <= 10.75F && x / 12.25F >= 0 && x / 12.25F < 9) {
				index = (int) Math.floor(x / 12.25F) + 10;
			}
		} else if (mouseY >= keyboardY + 33F && mouseY <= keyboardY + 47.5F) {
			if (x >= 0 && x <= 16.75F) {
				shakeTick = 0;
				onEnter();
				return true;
			} else if (x >= 104F && x <= 121F) {
				if (!isUnlocked() && !input.isEmpty()) {
					input = input.substring(0, input.length() - 1);
				}
				shakeTick = 0;
				return true;
			} else {
				x -= 18.25F;
				if (x % 12.25F <= 10.75F && x / 12.25F >= 0 && x / 12.25F < 7) {
					index = (int) Math.floor(x / 12.25F) + 19;
				}
			}
		}
		if (index != -1) {
			if (!isUnlocked() && menu.getData().getTotalLine() != 6 && flipTick == 0 && input.length() < 5) {
				input += "QWERTYUIOPASDFGHJKLZXCVBNM".charAt(index);
				typeTick = 5;
				shakeTick = 0;
			}
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
		if (key >= 65 && key <= 90) {
			if (!isUnlocked() && menu.getData().getTotalLine() != 6 && flipTick == 0 && input.length() < 5) {
				input += (char) key;
				typeTick = 5;
				shakeTick = 0;
			}
			return true;
		}
		if (key == 257) {
			shakeTick = 0;
			onEnter();
			return true;
		}
		if (key == 259) {
			if (!isUnlocked() && !input.isEmpty()) {
				input = input.substring(0, input.length() - 1);
				shakeTick = 0;
			}
			return true;
		}
		return super.keyPressed(key, p_231046_2_, p_231046_3_);
	}

	@Override
	public void onClose() {
		if (isUnlocked() && successTick == 0 && flipTick == 0 && !quitContainer) {
			quitContainer = true;
			return;
		}
		super.onClose();
	}

	public void onEnter() {
		if (menu.getData().getTotalLine() == 6 || isUnlocked()) {
			return;
		}
		if (input.length() == 5) {
			String lower = input.toLowerCase();
			boolean flag = false;
			for (String s : TEWordleChest.words[0]) {
				if (s.equals(lower)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				for (String s : TEWordleChest.words[1]) {
					if (s.equals(lower)) {
						flag = true;
						break;
					}
				}
			}
			if (!flag) {
				shakeTick = 12;
				errorType = true;
				errorTick = 20;
				return;
			}
			WordleChest.CHANNEL.sendToServer(new MessageMordleGuess(input));
			menu.getData().addState(new GuessState(input.toCharArray(), new CharState[] { CharState.GRAY,
					CharState.GRAY, CharState.GRAY, CharState.GRAY, CharState.GRAY }));
			input = "";
			flipTick = 50;
		} else {
			shakeTick = 12;
			errorType = false;
			errorTick = 20;
		}
	}

	public boolean isUnlocked() {
		if (menu.getData().getTotalLine() == 0) {
			return false;
		}
		GuessState state = menu.getData().getLine(menu.getData().getTotalLine() - 1);
		return state.state[0] == CharState.GREEN && state.state[1] == CharState.GREEN
				&& state.state[2] == CharState.GREEN && state.state[3] == CharState.GREEN
				&& state.state[4] == CharState.GREEN;

	}

	public GuessState getGuessState(int index) {
		int total = menu.getData().getTotalLine();
		if (index == total) {
			char[] chars = new char[5];
			CharState[] states = new CharState[5];
			for (int i = 0; i < input.length(); i++) {
				chars[i] = input.charAt(i);
				states[i] = CharState.GRAY;
			}
			return new GuessState(chars, states);
		} else if (index > total) {
			return new GuessState(new char[5], new CharState[5]);
		}
		return menu.getData().getLine(index);
	}

	public CharState getCharState(char c) {
		CharState state = CharState.GRAY;
		for (int i = 0; i < menu.getData().getTotalLine(); i++) {
			if (flipTick > 0 && i == menu.getData().getTotalLine() - 1) {
				return state;
			}
			GuessState s = menu.getData().getLine(i);
			for (int j = 0; j < 5; j++) {
				if (s.guess[j] == c && s.state[j].ordinal() > state.ordinal()) {
					state = s.state[j];
				}
			}
		}
		return state;
	}

	public float renderChar(float x, float y, char c, int color, Font font, float scale) {
		return renderChar(x, y, c, color, font, scale, scale);
	}

	public float renderChar(float x, float y, char c, int color, Font font, float xScale, float yScale) {
		Integer[] a = font.getChars().get((int) c);
		if (a != null) {
			drawTexture(font.getTextures().get(a[7]), x + a[4] * xScale, y + a[5] * yScale, a[2] * xScale,
					a[3] * yScale, a[0], a[1], a[2], a[3], color);
			return a[6] * xScale;
		}
		return 0;
	}

	public int getCharWidth(char c, Font font) {
		Integer[] a = font.getChars().get((int) c);
		if (a != null) {
			return a[6];
		}
		return 0;
	}

	public int getStringWidth(String s, Font font) {
		int w = 0;
		for (int i = 0; i < s.length(); i++) {
			w += getCharWidth(s.charAt(i), font);
		}
		return w;
	}

	public void drawTexture(ResourceLocation texture, float x, float y, float u, float v, float uWidth, float vHeight,
			float scale) {
		drawTexture(texture, x, y, uWidth * scale, vHeight * scale, u, v, uWidth, vHeight, 0xFFFFFFFF);
	}

	public void drawTexture(ResourceLocation texture, float x, float y, float u, float v, float uWidth, float vHeight,
			float scale, int color) {
		drawTexture(texture, x, y, uWidth * scale, vHeight * scale, u, v, uWidth, vHeight, color);
	}

	@SuppressWarnings("deprecation")
	public void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float u, float v,
			float uWidth, float vHeight, int color) {
		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		minecraft.getTextureManager().bind(texture);
		GlStateManager._enableBlend();
		GlStateManager._enableTexture();
		GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager._color4f(r, g, b, a);
		float f = 0.00390625F;
		GL11.glBegin(GL11.GL_QUADS);

		GL11.glTexCoord2d((u + uWidth) * f, (v + vHeight) * f);
		GL11.glVertex3d(x + width, y + height, 0);

		GL11.glTexCoord2d((u + uWidth) * f, v * f);
		GL11.glVertex3d(x + width, y, 0);

		GL11.glTexCoord2d(u * f, v * f);
		GL11.glVertex3d(x, y, 0);

		GL11.glTexCoord2d(u * f, (v + vHeight) * f);
		GL11.glVertex3d(x, y + height, 0);

		GL11.glEnd();
	}

	@SuppressWarnings("deprecation")
	public void drawRect(float x, float y, float width, float height, int color) {
		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		GlStateManager._disableTexture();
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager._color4f(r, g, b, a);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3d(x + width, y + height, 0);
		GL11.glVertex3d(x + width, y, 0);
		GL11.glVertex3d(x, y, 0);
		GL11.glVertex3d(x, y + height, 0);
		GL11.glEnd();
	}

	public void drawRoundedRect(float x, float y, float width) {
		drawTexture(TEXTURE, x, y, 172, 0, 4, 50, 0.25F);
		drawTexture(TEXTURE, x + width - 1, y, 176, 0, 4, 50, 0.25F);
//		drawRect(x + 1, y, width - 1, 12.5F, 0xFF000000);
	}

	public void drawErrorText(String text, float x, float y) {
		float width = getStringWidth(text, FONT_16) * 0.25F + 2;
		drawRoundedRect(x, y, width);
		x += 4;
		for (char c : text.toCharArray()) {
			x += renderChar(x, y + (12.5F - FONT_16.getLineHeight() * 0.25F) * 0.5F, c, 0xFFFFFFFF, FONT_16, 0.25F);
		}
	}

	public void drawBorder(float x, float y, float width, float height, float border, int color) {
		drawRect(x, y, width, border, color);
		drawRect(x, y + height - border, width, border, color);
		drawRect(x, y + border, border, height - border * 2, color);
		drawRect(x + width - border, y + border, border, height - border * 2, color);
	}
}
