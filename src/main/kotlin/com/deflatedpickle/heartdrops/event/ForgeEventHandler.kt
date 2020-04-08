package com.deflatedpickle.heartdrops.event

import com.deflatedpickle.heartdrops.HeartDrops
import com.deflatedpickle.heartdrops.capability.DropHearts
import com.deflatedpickle.heartdrops.configs.GeneralConfig
import com.deflatedpickle.heartdrops.init.Item
import com.deflatedpickle.heartdrops.item.CrystalHeart
import com.deflatedpickle.heartdrops.item.GoldenHeart
import com.deflatedpickle.heartdrops.item.Heart
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionUtils
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import kotlin.math.min

class ForgeEventHandler {
    @SubscribeEvent
    fun onEntityItemPickupEvent(event: EntityItemPickupEvent) {
        val itemStack = event.item.item
        val item = event.item.item.item

        if (item is Heart) {
            collectHearts(event, itemStack)
        }
    }

    @SubscribeEvent
    fun onLivingDropEvent(event: LivingDropsEvent) {
        var spawnItems = false
        var lootingLevel = 0

        if (event.entity.world.difficulty != GeneralConfig.dropDifficulty) return
        // They've chosen to install the mod... but never want hearts to drop
        // Totally defeats the purpose of having it, but whatever
        if (GeneralConfig.dropWhen == GeneralConfig.When.NEVER) return
        if (!GeneralConfig.dropHardcore && event.entity.world.minecraftServer!!.isHardcore) return

        if (!event.entity.world.isRemote) {
            val source = event.source.trueSource
            println("${source!!::class}")

            when (GeneralConfig.dropWhen) {
                GeneralConfig.When.HURT -> {
                    if (source is EntityLivingBase && source.health < source.maxHealth) {
                        spawnItems = true
                    } else if (source is EntityArrow) {
                        val entity = source.shootingEntity
                        if (entity is EntityLiving) {
                            if (entity.health < entity.maxHealth) {
                                spawnItems = true
                            }
                        }
                    }

                }
                GeneralConfig.When.ALWAYS -> spawnItems = true
                GeneralConfig.When.NEVER -> return
            }

            lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByID(21)!!, event.entityLiving.heldItemMainhand)

            var dropAmount = 0

            val entity = event.entityLiving
            val dropHearts = DropHearts.isCapable(entity)
            if (dropHearts != null) {
                if (dropHearts.doesDropHearts()) {
                    dropAmount = dropHearts.dropAmount
                }
            }

            val heartList = mutableListOf<EntityItem>()

            for (i in 0..dropAmount * (lootingLevel + 1)) {
                if (GeneralConfig.goldHeart.drop) {
                    val bound = (GeneralConfig.goldHeart.chance + GeneralConfig.goldHeart.lootingMultiplier - ((lootingLevel + 1) * GeneralConfig.goldHeart.lootingMultiplier)) / (lootingLevel + 1)
                    if (HeartDrops.random.nextInt(0, bound) == 0) {
                        heartList.add(EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, ItemStack(GoldenHeart(), 1)))
                    }
                }

                if (GeneralConfig.crystalHeart.drop) {
                    val bound = (GeneralConfig.crystalHeart.chance + GeneralConfig.crystalHeart.lootingMultiplier - ((lootingLevel + 1) * GeneralConfig.crystalHeart.lootingMultiplier)) / (lootingLevel + 1)
                    if (HeartDrops.random.nextInt(0, bound) == 0) {
                        val itemStack = ItemStack(CrystalHeart(), 1)
                        // How helpful it is to tell us what's bad...
                        // Otherwise, you'd be stuck with good and bad effects
                        // I'm not writing more code to filter them out :^)
                        ForgeRegistries.POTIONS.valuesCollection.filter { !it.isInstant && !it.isBadEffect }.toList().apply {
                            PotionUtils.appendEffects(itemStack, mutableListOf(PotionEffect(
                                    this[HeartDrops.random.nextInt(this.size)],
                                    // It should last for *enough* time to get use out of it
                                    // TODO: Make crystal heart effects further customisable
                                    HeartDrops.random.nextInt(20 * 20, 20 * 30)
                            )))
                        }
                        heartList.add(EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, itemStack))
                    }
                }

                if (GeneralConfig.dropWhen == GeneralConfig.When.HURT) {
                    when (GeneralConfig.dropAmount) {
                        GeneralConfig.DropAmount.UNTIL_FULL_HEALTH -> {
                            heartList.add(EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, ItemStack(when {
                                event.entityLiving.health + 2 <= event.entityLiving.maxHealth -> {
                                    Item.heart
                                }
                                event.entityLiving.health + 1 <= event.entityLiving.maxHealth -> {
                                    Item.half_heart
                                }
                                else -> {
                                    Items.AIR
                                }
                            }, 1)))

                        }
                        else -> {
                        }
                    }
                } else {
                    heartList.add(EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, ItemStack(Item.heart, 1)))
                }
            }

            for (i in heartList.subList(0, min(heartList.size, GeneralConfig.dropRange))) {
                entity.world.spawnEntity(i)
            }
        }
    }

    fun collectHearts(event: EntityItemPickupEvent, itemStack: ItemStack) {
        val item = itemStack.item

        if (item is Heart) {
            for (i in 0..itemStack.count) {
                item.type.collect(event)
            }
            itemStack.count = 0
        }
    }

    @SubscribeEvent
    fun onAttachCapabilitiesEventEntity(event: AttachCapabilitiesEvent<Entity>) {
        if (event.`object` !is EntityPlayer) {
            event.addCapability(DropHearts.NAME, DropHearts.Provider())
        }
    }
}
