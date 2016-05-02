package org.redfrog404.mobycraft.commands.common;

import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;

import org.redfrog404.mobycraft.api.MobycraftBasicCommands;
import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.structure.BoxContainer;

import net.minecraft.util.EnumChatFormatting;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.async.ResultCallbackTemplate;

public class StatisticsResultCallback extends
		ResultCallbackTemplate<StatisticsResultCallback, Statistics> {

	boolean sendMessages = true;
	String containerID = "";
	private final MobycraftBasicCommands basicCommands;
	private final MobycraftContainerListCommands listCommands;

	public StatisticsResultCallback(String containerID, boolean sendMessages,
									MobycraftContainerListCommands listCommands,
									MobycraftBasicCommands basicCommands) {
		this.sendMessages = sendMessages;
		this.containerID = containerID;
		this.basicCommands = basicCommands;
		this.listCommands = listCommands;
	}

	@Override
	public void onNext(Statistics stats) {
		BoxContainer boxContainer = listCommands.getBoxContainerWithID(containerID);
		Container container = listCommands.getFromAllWithName(boxContainer.getName());

		NumberFormat formatter = new DecimalFormat("#0.00");

		double memoryUsage = Double.parseDouble(formatter
				.format((((Integer) stats.getMemoryStats().get("usage"))
						.doubleValue() / ((Integer) stats.getMemoryStats().get(
						"limit")).doubleValue()) * 100D));
		Object totalUsage = ((LinkedHashMap) stats.getCpuStats().get(
				"cpu_usage")).get("total_usage");
		double totalUsageDouble = 0D;
		if (totalUsage instanceof Integer) {
			totalUsageDouble = ((Integer) totalUsage).doubleValue();
		} else if (totalUsage instanceof Long) {
			totalUsageDouble = ((Long) totalUsage).doubleValue();
		}
		double cpuUsage = Double.parseDouble(formatter.format(totalUsageDouble
				/ ((Long) stats.getCpuStats().get("system_cpu_usage"))
						.doubleValue() * 100D));

		boxContainer.setMemoryUsage(memoryUsage);
		boxContainer.setCpuUsage(cpuUsage);

		if (sendMessages) {
			basicCommands.printContainerInfo(boxContainer, container);
			sendMessage(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD
					+ "Memory Usage: " + EnumChatFormatting.RESET + memoryUsage
					+ "%");
			sendMessage(EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD
					+ "CPU Usage: " + EnumChatFormatting.RESET + cpuUsage + "%");
		}

		try {
			super.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
