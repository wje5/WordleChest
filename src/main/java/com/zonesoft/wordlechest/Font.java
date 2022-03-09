package com.zonesoft.wordlechest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class Font {
	private int size, lineHeight, base, scaleW, scaleH, pages, charsCount, kerningsCount;
	private int[] padding, spacing;

	// x y width height xoffset yoffset xadvance page
	private Map<Integer, Integer[]> chars = new HashMap<Integer, Integer[]>();

	// long:(first<<32 + second) amount
	private Map<Long, Integer> kernings = new HashMap<Long, Integer>();
	private List<ResourceLocation> textures = new ArrayList<ResourceLocation>();

	public Font(String name, int fileSize) {
		InputStream stream = null;
		try {
			stream = Minecraft.getInstance().getResourceManager()
					.getResource(new ResourceLocation("wordlechest:fonts/" + name + ".bin")).getInputStream();
			ByteBuffer buffer = readToBuffer(stream, fileSize);
			buffer.clear();
			boolean[] flags = byteToBool8(buffer.get());
			size = buffer.getInt();
			lineHeight = buffer.getInt();
			base = buffer.getInt();
			scaleW = buffer.getInt();
			scaleH = buffer.getInt();
			pages = buffer.getInt();
			charsCount = buffer.getInt();
			kerningsCount = buffer.getInt();
			padding = new int[] { buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt() };
			spacing = new int[] { buffer.getInt(), buffer.getInt() };
			for (int i = 0; i < charsCount; i++) {
				int id = buffer.getInt();
				Integer[] a = new Integer[] { buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt(),
						buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt() };
				chars.put(id, a);
			}
			for (int i = 0; i < kerningsCount; i++) {
				long l = buffer.getInt();
				l <<= 32;
				l |= buffer.getInt();
				kernings.put(l, buffer.getInt());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		for (int i = 0; i < pages; i++) {
			textures.add(new ResourceLocation("wordlechest:fonts/" + name + +(i + 1) + ".png"));
		}
	}

	public static ByteBuffer readToBuffer(InputStream stream, int size) throws IOException {
		ByteBuffer bytebuffer = ByteBuffer.allocateDirect(size + 1);
		ReadableByteChannel channel = Channels.newChannel(stream);
		while (channel.read(bytebuffer) != -1) {
		}
		return bytebuffer;
	}

	public static boolean[] byteToBool8(byte b) {
		boolean[] bools = new boolean[8];
		for (int i = 0; i < 7; i++) {
			bools[i] = (b & 0x80) == 0x80;
			b <<= 1;
		}
		bools[7] = (b & 1) == 1;
		return bools;
	}

	public Map<Integer, Integer[]> getChars() {
		return chars;
	}

	public List<ResourceLocation> getTextures() {
		return textures;
	}

	public Map<Long, Integer> getKernings() {
		return kernings;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public int getBase() {
		return base;
	}

	public int getScaleW() {
		return scaleW;
	}

	public int getScaleH() {
		return scaleH;
	}

	public int getSize() {
		return size;
	}

	public int getPages() {
		return pages;
	}

	public int getCharsCount() {
		return charsCount;
	}

	public int getKerningsCount() {
		return kerningsCount;
	}

	public int[] getSpacing() {
		return spacing;
	}

	public int[] getPadding() {
		return padding;
	}

}
