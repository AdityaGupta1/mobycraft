package org.redfrog404.mobycraft.api;

import java.util.List;

import org.redfrog404.mobycraft.model.Image;

public interface MobycraftImageCommands {
	public void images();

	public Image getImageWithName(String name);

	public List<Image> getImages() ;

	public void removeImage();

	public void removeAllImages();
}
