package org.redfrog404.mobycraft.commands.dockerjava;

import static org.redfrog404.mobycraft.commands.common.MainCommand.arg1;
import static org.redfrog404.mobycraft.commands.common.MainCommand.args;
import static org.redfrog404.mobycraft.utils.MessageSender.sendBarMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendConfirmMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendFeedbackMessage;
import static org.redfrog404.mobycraft.utils.MessageSender.sendMessage;

import java.util.List;
import java.util.stream.Collectors;

import org.redfrog404.mobycraft.api.MobycraftContainerListCommands;
import org.redfrog404.mobycraft.api.MobycraftImageCommands;
import org.redfrog404.mobycraft.utils.Utils;

import net.minecraft.util.EnumChatFormatting;

import org.redfrog404.mobycraft.model.Image;

import javax.inject.Inject;

public class ImageCommands implements MobycraftImageCommands {

	private final MobycraftContainerListCommands listCommands;

	@Inject
	public ImageCommands(MobycraftContainerListCommands listCommands) {
		this.listCommands = listCommands;
	}

	public void images() {
		List<Image> images = getImages();

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
			listCommands.getDockerClient().removeImageCmd(image.getId()).withForce().exec();
		}
		sendConfirmMessage("Removed all images.");
	}

	public void removeImage() {
		if (Utils.checkIfArgIsNull(args, 0)) {
			sendErrorMessage("Image name not specified! Command is used as /docker rmi <name> .");
			return;
		}

		try {
			listCommands.getDockerClient().removeImageCmd(getImageWithName(arg1).getId())
					.withForce().exec();
			sendConfirmMessage("Removed image with name \"" + arg1 + "\"");
		} catch (NullPointerException exception) {
			sendErrorMessage("No image exists with the name \"" + arg1 + "\"");
		}
	}

	private List<Image> convertImageList(List<com.github.dockerjava.api.model.Image> imagesDC) {
		List<Image> images = imagesDC.stream()
				.map(image -> new Image(
						image.getCreated(),
						image.getId(),
						image.getParentId(),
						image.getRepoTags(),
						image.getSize(),
						image.getVirtualSize()
				))
				.collect(Collectors.toList());
		return images;
	}

	public List<Image> getImages() {
		List<com.github.dockerjava.api.model.Image> imagesDC = listCommands.getDockerClient().listImagesCmd().exec();
		return convertImageList(imagesDC);
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
