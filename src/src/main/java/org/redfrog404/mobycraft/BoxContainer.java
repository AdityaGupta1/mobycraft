package org.redfrog404.mobycraft;

import net.minecraft.util.BlockPos;

public class BoxContainer {
	
	private BlockPos position;
	private String ID;
	private String name;
	private String image;
	
	public BoxContainer(BlockPos position, String ID, String name, String image) {
		this.position = position;
		this.ID = ID;
		this.name = name;
		this.image = image;
	}
	
	public BlockPos getPosition(){
		return position;
	}
	
	public String getID(){
		return ID;
	}
	
	public String getShortID(){
		return ID.substring(0, 12);
	}
	
	public void setPosition(BlockPos newPos) {
		position = newPos;
	}
	
	public String getName(){
		return name;
	}
	
	public String getImage(){
		return image;
	}

}
