package org.redfrog404.mobycraft.entity;

import static org.redfrog404.mobycraft.utils.MessageSender.sendErrorMessage;

import java.util.Calendar;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.redfrog404.mobycraft.api.MobycraftCommandsFactory;
import org.redfrog404.mobycraft.main.Mobycraft;

import com.github.dockerjava.api.model.Container;

public class EntityChaosMonkey extends EntityAmbientCreature {
	/** Coordinates of where the monkey spawned. */
	private BlockPos spawnPosition;

	/** Value to set chaosCountdown to after a container is removed. */
	private int maxChaosCountdown = 50;
	
	/** Time in ticks before another container can be removed by the monkey. */
	private int chaosCountdown = maxChaosCountdown;
	
	/** The maximum distance that can be between the monkey and the player before the monkey is teleported to the player. */
	private int maxDistance = 25;
	
	MobycraftCommandsFactory factory = MobycraftCommandsFactory.getInstance();

	public EntityChaosMonkey(World worldIn) {
		super(worldIn);
		this.setSize(0.5F, 0.9F);
	}

	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte) 0));
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume() {
		return 0.1F;
	}

	/**
	 * Gets the pitch of living sounds in living entities.
	 */
	protected float getSoundPitch() {
		return super.getSoundPitch() * 0.95F;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	protected String getLivingSound() {
		return null;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	protected String getHurtSound() {
		return null;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	protected String getDeathSound() {
		return null;
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities
	 * when colliding.
	 */
	public boolean canBePushed() {
		return false;
	}

	protected void collideWithEntity(Entity p_82167_1_) {
	}

	protected void collideWithNearbyEntities() {
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth)
				.setBaseValue(6.0D);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {
		super.onUpdate();
		this.motionY *= 0.6000000238418579D;
		
		if (chaosCountdown > 0) {
			chaosCountdown--;
			return;
		}
		
		ICommandSender sender = Mobycraft.getMainCommand().sender;
		if (sender != null) {
			BlockPos senderPos = sender.getPosition();
			if (Math.sqrt(this.getDistanceSq(senderPos)) > maxDistance) {
				this.setLocationAndAngles(senderPos.getX(), senderPos.getY(), senderPos.getZ(), 0, 0);
			}
		}
		
		World world = this.worldObj;
		if (world.getTileEntity(this.getPosition()) == null) {
			return;
		}
		
		TileEntity entity = world.getTileEntity(this.getPosition());
		if (!(entity instanceof TileEntitySign)) {
			return;
		}
		
		TileEntitySign sign = (TileEntitySign) entity;
		
		if (!sign.signText[0].getUnformattedText().contains("Name:")) {
			return;
		}

		String name = sign.signText[1].getUnformattedText()
				.concat(sign.signText[2].getUnformattedText().concat(sign.signText[3].getUnformattedText()));

		if (factory.getListCommands().getWithName(name) == null) {
			return;
		}

		Container container = factory.getListCommands().getWithName(name);
		Mobycraft.getMainCommand().getDockerClient().removeContainerCmd(container.getId()).withForce().exec();
		if (!world.isRemote) {
			sendErrorMessage("Oh no! The Chaos Monkey has destroyed the container \"" + name + "\"!");
		}
		
		chaosCountdown = maxChaosCountdown;

		factory.getBuildCommands().updateContainers(false);
	}

	protected void updateAITasks() {
		super.updateAITasks();
		BlockPos blockpos = new BlockPos(this);
		BlockPos blockpos1 = blockpos.up();

		{
			if (this.spawnPosition != null
					&& (!this.worldObj.isAirBlock(this.spawnPosition) || this.spawnPosition
							.getY() < 1)) {
				this.spawnPosition = null;
			}

			if (this.spawnPosition == null
					|| this.rand.nextInt(30) == 0
					|| this.spawnPosition.distanceSq(
							(double) ((int) this.posX),
							(double) ((int) this.posY),
							(double) ((int) this.posZ)) < 4.0D) {
				this.spawnPosition = new BlockPos((int) this.posX
						+ this.rand.nextInt(7) - this.rand.nextInt(7),
						(int) this.posY + this.rand.nextInt(6) - 2,
						(int) this.posZ + this.rand.nextInt(7)
								- this.rand.nextInt(7));
			}

			double d0 = (double) this.spawnPosition.getX() + 0.5D - this.posX;
			double d1 = (double) this.spawnPosition.getY() + 0.1D - this.posY;
			double d2 = (double) this.spawnPosition.getZ() + 0.5D - this.posZ;
			this.motionX += (Math.signum(d0) * 0.5D - this.motionX) * 0.10000000149011612D;
			this.motionY += (Math.signum(d1) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
			this.motionZ += (Math.signum(d2) * 0.5D - this.motionZ) * 0.10000000149011612D;
			float f = (float) (MathHelper.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) - 90.0F;
			float f1 = MathHelper.wrapAngleTo180_float(f - this.rotationYaw);
			this.moveForward = 0.5F;
			this.rotationYaw += f1;
		}
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they
	 * walk on. used for spiders and wolves to prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {
		return false;
	}

	public void fall(float distance, float damageMultiplier) {
	}

	protected void updateFallState(double y, boolean onGroundIn, Block blockIn,
			BlockPos pos) {
	}

	/**
	 * Return whether this entity should NOT trigger a pressure plate or a
	 * tripwire.
	 */
	public boolean doesEntityNotTriggerPressurePlate() {
		return true;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else {
			return super.attackEntityFrom(source, amount);
		}
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound tagCompund) {
		super.readEntityFromNBT(tagCompund);
		this.dataWatcher.updateObject(16,
				Byte.valueOf(tagCompund.getByte("BatFlags")));
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		super.writeEntityToNBT(tagCompound);
		tagCompound.setByte("BatFlags",
				this.dataWatcher.getWatchableObjectByte(16));
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this
	 * entity.
	 */
	public boolean getCanSpawnHere() {
		BlockPos blockpos = new BlockPos(this.posX,
				this.getEntityBoundingBox().minY, this.posZ);

		if (blockpos.getY() >= this.worldObj.getSeaLevel()) {
			return false;
		} else {
			int i = this.worldObj.getLightFromNeighbors(blockpos);
			int j = 4;

			if (this.isDateAroundHalloween(this.worldObj.getCurrentDate())) {
				j = 7;
			} else if (this.rand.nextBoolean()) {
				return false;
			}

			return i > this.rand.nextInt(j) ? false : super.getCanSpawnHere();
		}
	}

	private boolean isDateAroundHalloween(Calendar p_175569_1_) {
		return p_175569_1_.get(2) + 1 == 10 && p_175569_1_.get(5) >= 20
				|| p_175569_1_.get(2) + 1 == 11 && p_175569_1_.get(5) <= 3;
	}

	public float getEyeHeight() {
		return this.height / 2.0F;
	}
}