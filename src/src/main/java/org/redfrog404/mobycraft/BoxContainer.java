package org.redfrog404.mobycraft;

import net.minecraft.util.BlockPos;

public class BoxContainer {
	
	private BlockPos position;
	private String ID;
	
	public BoxContainer(BlockPos position, String ID) {
		this.position = position;
		this.ID = ID;
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
	
	public void setID(String newID) {
		ID = newID;
	}

}
