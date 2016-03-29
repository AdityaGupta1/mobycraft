package org.redfrog404.mobycraft.utils;

import static org.redfrog404.mobycraft.commands.ContainerListCommands.getBoxContainerWithID;
import static org.redfrog404.mobycraft.commands.ContainerListCommands.getFromAllWithName;
import static org.redfrog404.mobycraft.commands.MainCommand.arg1;
import static org.redfrog404.mobycraft.utils.SendMessagesToCommandSender.sendMessage;
import net.minecraft.util.EnumChatFormatting;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

public class StatisticsResultCallback extends ResultCallbackTemplate<StatisticsResultCallback, Statistics> {

	@Override
	public void onNext(Statistics arg0) {
		BoxContainer boxContainer = getBoxContainerWithID(arg1);
		Container container = getFromAllWithName(boxContainer.getName());
		
		sendMessage(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD
				+ "=========== Container Information ===========");
		sendMessage(EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD
				+ "Name: " + EnumChatFormatting.RESET + boxContainer.getName());
		sendMessage(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
				+ "Image: " + EnumChatFormatting.RESET
				+ boxContainer.getImage());
		sendMessage(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD
				+ "ID: " + EnumChatFormatting.RESET + boxContainer.getShortID());
		sendMessage(EnumChatFormatting.LIGHT_PURPLE + ""
				+ EnumChatFormatting.BOLD + "Status: "
				+ EnumChatFormatting.RESET + container.getStatus());
	}

}
