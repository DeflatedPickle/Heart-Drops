package com.deflatedpickle.heartdrops.events;

import com.deflatedpickle.heartdrops.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ForgeEventHandler {
    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        EntityPlayer player = null;
        Boolean spawnItems = false;

        if (!event.getEntity().world.isRemote) {
            if (event.getSource().getImmediateSource() instanceof EntityPlayer) {
                player = (EntityPlayer) event.getSource().getImmediateSource();
            } else if(event.getSource().getTrueSource() instanceof EntityPlayer) {
                player = (EntityPlayer) event.getSource().getTrueSource();
            }

            if (player != null) {
                if (player.getHealth() < player.getMaxHealth()) {
                    spawnItems = true;
                }
            }

            if (spawnItems) {
                EntityItem item = new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(ModItems.heart, 1));
                entity.world.spawnEntity(item);
            }
        }
    }
}
