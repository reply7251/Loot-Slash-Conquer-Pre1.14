package com.thexfactor117.lsc.player;

import java.util.Iterator;
import java.util.List;

import com.thexfactor117.lsc.capabilities.implementation.LSCPlayerCapability;
import com.thexfactor117.lsc.loot.Attribute;
import com.thexfactor117.lsc.util.LSCDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * 
 * @author TheXFactor117
 *
 */
public class WeaponUtils 
{
	/** Called to use the current stack's attributes. Called from LivingAttackEvent and projectiles. */
	public static void useWeaponAttributes(float damage, EntityLivingBase attacker, EntityLivingBase enemy, ItemStack stack, NBTTagCompound nbt)
	{		

		/*
		 * for (all weapon attributes)
		 * {
		 *     if (hasAttribute && instanceof AttributeWeapon)
		 *     {
		 *         attribute.executeAttribute(...);
		 *     }
		 * }
		 */
		
		
		if (Attribute.DURABLE.hasAttribute(nbt) && Math.random() < Attribute.DURABLE.getAmount(nbt)) stack.setItemDamage(stack.getItemDamage() - 1);
		if (Attribute.FIRE.hasAttribute(nbt)) 
		{
			enemy.hurtResistantTime = 0;
			// change damage type to custom LSC damage type?
			enemy.attackEntityFrom(DamageSource.ON_FIRE, (float) Attribute.FIRE.getAmount(nbt));
		}
		if (Attribute.FROST.hasAttribute(nbt))
		{
			enemy.hurtResistantTime = 0;
			enemy.attackEntityFrom(LSCDamageSource.causeFrostDamage(attacker), (float) Attribute.FROST.getAmount(nbt));
			enemy.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20 * 3, 5));
		}
		if (Attribute.LIGHTNING.hasAttribute(nbt))
		{
			enemy.hurtResistantTime = 0;
			enemy.attackEntityFrom(LSCDamageSource.causeLightningDamage(attacker), (float) Attribute.LIGHTNING.getAmount(nbt));
			
			// remove half the lightning damage dealt from mana.
			if (enemy instanceof EntityPlayer)
			{
				LSCPlayerCapability cap = PlayerUtil.getLSCPlayer((EntityPlayer) enemy);
				
				if (cap != null)
				{
					cap.decreaseMana((int) (Attribute.LIGHTNING.getAmount(nbt) / 2));
				}
			}
		}
		if (Attribute.POISON.hasAttribute(nbt)) 
		{
			enemy.hurtResistantTime = 0;
			enemy.attackEntityFrom(LSCDamageSource.causePoisonDamage(attacker), (float) Attribute.POISON.getAmount(nbt));
		}
		if (Attribute.LIFE_STEAL.hasAttribute(nbt)) 
		{
			attacker.setHealth((float) (attacker.getHealth() + (damage * Attribute.LIFE_STEAL.getAmount(nbt))));
		}
		if (Attribute.MANA_STEAL.hasAttribute(nbt))
		{
			LSCPlayerCapability cap = PlayerUtil.getLSCPlayer((EntityPlayer) attacker);
			
			if (cap != null)
			{
				// adds mana to the player each attack.
				cap.increaseMana((int) (Attribute.MANA_STEAL.getAmount(nbt) * damage));
			}
		}
		if (Attribute.CHAINED.hasAttribute(nbt))
		{
			double radius = Attribute.CHAINED.getAmount(nbt);
			World world = enemy.getEntityWorld();
			List<EntityLivingBase> entityList = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(attacker.posX - radius, attacker.posY - radius, attacker.posZ - radius, attacker.posX + radius, attacker.posY + radius, attacker.posZ + radius));
			Iterator<EntityLivingBase> iterator = entityList.iterator();
			
			while (iterator.hasNext())
			{
                Entity entity = (Entity) iterator.next();
				
                // IF PLAYER IS THE ATTACKER
				if (entity instanceof EntityLivingBase && attacker instanceof EntityPlayer && !(entity instanceof EntityPlayer) && !(entity instanceof EntityAnimal) && !(entity instanceof EntitySlime))
				{
					entity.hurtResistantTime = 0;
					entity.attackEntityFrom(LSCDamageSource.causeChainedDamage(attacker), (float) (damage * 0.25));
				}
				// IF A MOB IS THE ATTACKER
				else if (entity instanceof EntityPlayer && attacker instanceof EntityMob)
				{
					entity.hurtResistantTime = 0;
					entity.attackEntityFrom(LSCDamageSource.causeChainedDamage(attacker), (float) (damage * 0.25));
				}
			}
		}
		if (Attribute.VOID.hasAttribute(nbt) && Math.random() < Attribute.VOID.getAmount(nbt)) enemy.setHealth(0.1F);
	}
}
