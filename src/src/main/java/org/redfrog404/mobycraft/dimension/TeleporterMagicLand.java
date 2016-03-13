package org.redfrog404.mobycraft.dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TeleporterMagicLand extends Teleporter {

	private final WorldServer worldServerInstance;
	private final Random random;
	private final LongHashMap destinationCoordinateCache = new LongHashMap();
	private final List destinationCoordinateKeys = new ArrayList();

	public TeleporterMagicLand(WorldServer worldserver) {
		super(worldserver);
		this.worldServerInstance = worldserver;
		this.random = new Random(worldserver.getSeed());

	}

	public void placeInPortal(Entity entity, float rotationYaw) {

		int i = MathHelper.floor_double(entity.posX);
		int j = MathHelper.floor_double(entity.posY) - 1;
		int k = MathHelper.floor_double(entity.posZ);
		byte b0 = 1;
		byte b1 = 0;

		for (int l = -2; l <= 2; ++l) {
			for (int i1 = -2; i1 <= 2; ++i1) {
				for (int j1 = -1; j < 3; ++j1) {
					int k1 = i + i1 * b0 + l * b1;
					int l1 = j + j1;
					int i2 = k + i1 * b1 - l * b0;
					boolean flag = j1 < 0;
				}
			}
		}

		entity.setLocationAndAngles((double) i, ((double) j) + 2, (double) k,
				entity.rotationYaw, 0.0F);
		entity.motionX = entity.motionY = entity.motionZ = 0.0D;
		
		while (entity.worldObj.getBlockState(entity.getPosition()).getBlock().isFullCube()) {
			entity.moveEntity(0, 1, 0);
		}

	}

	public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
		this.placeInPortal(entity, rotationYaw);
		return true;
	}
}