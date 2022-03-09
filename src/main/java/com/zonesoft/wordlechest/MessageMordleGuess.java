package com.zonesoft.wordlechest;

import java.util.function.Supplier;

import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageMordleGuess {
	private String guess;

	public MessageMordleGuess(String guess) {
		this.guess = guess;
	}

	public MessageMordleGuess(PacketBuffer buffer) {
		guess = buffer.readUtf();
	}

	public void toBytes(PacketBuffer buffer) {
		buffer.writeUtf(guess);
	}

	public void handler(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Container container = ctx.get().getSender().containerMenu;
			if (container instanceof ContainerWordleChest) {
				((ContainerWordleChest) container).getTileEntity().guess(guess);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
