package org.redfrog404.mobycraft.utils;

import static org.redfrog404.mobycraft.commands.MainCommand.sender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class SendMessagesToCommandSender {
	
	public static void sendMessage(String message) {
		sender.addChatMessage(new ChatComponentText(message));
	}

	public static void sendErrorMessage(String message) {
		sendMessage(EnumChatFormatting.DARK_RED + message);
	}

	public static void sendConfirmMessage(String message) {
		sendMessage(EnumChatFormatting.GREEN + message);
	}

	public static void sendFeedbackMessage(String message) {
		sendMessage(EnumChatFormatting.GOLD + message);
	}

	public static void sendBarMessage(EnumChatFormatting color) {
		sendMessage(color + "" + EnumChatFormatting.BOLD
				+ "=============================================");
	}

}
