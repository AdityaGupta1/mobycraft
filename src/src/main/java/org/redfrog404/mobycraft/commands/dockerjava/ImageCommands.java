package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.args;
import static org.redfrog404.mobycraft.commands.dockerjava.MainCommand.getDockerClient;
import static org.redfrog404.mobycraft.utils.MessageSender.sendBarMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.util.List;

import org.redfrog404.mobycraft.api.MobycraftImageCommands;

import net.minecraft.util.EnumChatFormatting;

import com.github.dockerjava.api.model.Image;

public class ImageCommands implements MobycraftImageCommands {
	
	public void images() {
		List<Image> images = getDockerClient().listImagesCmd().exec();

		if (images.size() == 0) {
			sendFeedbackMessage("No images currently installed.");
			return;
		}

		sendBarMessage(EnumChatFormatting.BLUE);
		sendMessage(EnumChatFormatting.AQUA + "Repository"
				+ EnumChatFormatting.RESET + ", " + EnumChatFormatting.GOLD
				+ "Tag" + EnumChatFormatting.RESET + ", "
				+ EnumChatFormatting.GREEN + "Size");
		sendBarMessage(EnumChatFormatting.BLUE);

		String tag = "";

		for (Image image : images) {
			String message = "";
			for (String name : image.getRepoTags()) {
				if (image.getRepoTags()[0].equals(name)) {
					message += EnumChatFormatting.AQUA + name.split(":")[0];
				} else {
					message += ", " + name.split(":")[0];
				}
				tag = name.split(":")[1];
			}
			message += EnumChatFormatting.RESET + ", "
					+ EnumChatFormatting.GOLD + tag + EnumChatFormatting.RESET
					+ ", " + EnumChatFormatting.GREEN
					+ Utils.imageSizeConversion(image.getSize());
			sendMessage(message);
		}
	}

	public void removeAllImages() {
		sendFeedbackMessage("Working on it...");
		if (getImages().size() < 1) {
			sendFeedbackMessage("No images currently installed.");
			return;
		}
		for (Image image : getImages()) {
			getDockerClient().removeImageCmd(image.getId()).withForce().exec();
		}
		sendConfirmMessage("Removed all images.");
	}

	public void removeImage() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Image name not specified! Command is used as /docker rmi <name> .");
			return;
		}

		try {
			getDockerClient().removeImageCmd(getImageWithName(arg1).getId())
					.withForce().exec();
			sendConfirmMessage("Removed image with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No image exists with the name \"" + arg1 + "\"");
		}
	}

	public List<Image> getImages() {
		return getDockerClient().listImagesCmd().exec();
	}

	public Image getImageWithName(String name) {
		for (Image image : getImages()) {
			if (image.getRepoTags()[0].split(":")[0].equals(name)) {
				return image;
			}
		}
		return null;
	}

}
