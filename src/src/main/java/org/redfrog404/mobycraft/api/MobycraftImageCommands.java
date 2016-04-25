package org.redfrog404.mobycraft.api;

import java.util.List;

import com.github.dockerjava.api.model.Image;

public interface MobycraftImageCommands {
	public void images();

	public void removeAllImages();

	public void removeImage();

	public List<Image> getImages() ;

	public Image getImageWithName(String name);


}
