package org.redfrog404.mobycraft;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DockerCommands implements ICommand {
	private List aliases;

	Map<String, Integer> argNumbers = new HashMap<String, Integer>();

	ICommandSender sender;

	public DockerCommands() {
		this.aliases = new ArrayList();
		this.aliases.add("docker");
		argNumbers.put("help", 0);
		argNumbers.put("ps", 1);
	}

	@Override
	public String getCommandName() {
		return "docker";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "docker <command | help>";
	}

	@Override
	public List getCommandAliases() {
		return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		this.sender = sender;

		if (args.length == 0) {
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD
					+ "For help with this command, use /docker help"));
			return;
		}

		switch (argNumbers.get(args[0].toLowerCase())) {
		case 0:
			help();
			break;
		case 1:
			ps();
			break;
		}

	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}

	@Override
	public int compareTo(ICommand command) {
		return 0;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender,
			String[] args, BlockPos pos) {
		return null;
	}

	private void help() {
		sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA
				+ "" + EnumChatFormatting.BOLD
				+ "================ Docker Help ================"));
		sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.DARK_GREEN + "/docker help - "
						+ EnumChatFormatting.GOLD + "Brings up this help page"));
		sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.DARK_GREEN + "/docker ps - "
						+ EnumChatFormatting.GOLD
						+ "Makes a box for each container"));
	}

	private void ps() {
		String jsonString = "[{\"Id\": \"8dfafdbc3a40\", \"Names\":[\"/boring_feynman\"], \"Image\": \"ubuntu:latest\"}, {\"Id\": \"9cd87474be90\", \"Names\":[\"/coolName\"], \"Image\": \"ubuntu:latest\"}]"; // Example JSON code
		JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
		JsonArray array = jsonReader.readArray();
		JsonObject object = array.getJsonObject(0);
		JsonArray nameArray = object.getJsonArray("Names");
		String name = nameArray.getString(0);
		sender.addChatMessage(new ChatComponentText(name));

	}
}