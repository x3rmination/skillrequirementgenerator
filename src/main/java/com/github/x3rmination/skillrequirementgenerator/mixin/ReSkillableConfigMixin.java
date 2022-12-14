package com.github.x3rmination.skillrequirementgenerator.mixin;

import com.github.x3rmination.skillrequirementgenerator.SkillReqConfig;
import floris0106.rereskillablerereforked.common.Config;
import floris0106.rereskillablerereforked.common.skills.Requirement;
import floris0106.rereskillablerereforked.common.skills.Skill;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Config.class)
public abstract class ReSkillableConfigMixin {
    @Shadow(remap = false) private static Config config;
    @Inject(method = "load", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.AFTER), remap = false)
    private static void loadMixin(CallbackInfo ci) {
        ForgeRegistries.ITEMS.getEntries().forEach(entry -> {
            ItemStack stack = entry.getValue().getDefaultInstance();
            long damage = Math.round(stack.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum());
            if (damage > 0) {
                int level = (int) Math.min((Math.pow(damage, SkillReqConfig.damage_power.get())) / SkillReqConfig.damage_divisor.get(), SkillReqConfig.max_level.get());
                if (level > 0) {
                    Requirement[] requirements = new Requirement[1];
                    requirements[0] = new Requirement(Skill.ATTACK, level);
                    try {
                        ((Map<ResourceLocation, Requirement[]>) config.getClass().getDeclaredField("skillLocks").get(config)).put(entry.getValue().getRegistryName(), requirements);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
            int armor = 0;
            for (EquipmentSlotType type : EquipmentSlotType.values()) {
                if (type.getType().equals(EquipmentSlotType.Group.ARMOR)) {
                    armor = (int) Math.round(stack.getAttributeModifiers(type).get(Attributes.ARMOR).stream().mapToDouble(AttributeModifier::getAmount).sum());
                    if (armor > 0) {
                        break;
                    }
                }
            }
            if (armor > 0) {
                int level = (int) Math.min((Math.pow(armor, SkillReqConfig.armor_power.get())) / SkillReqConfig.armor_divisor.get(), SkillReqConfig.max_level.get());
                if(level > 0) {
                    Requirement[] requirements = new Requirement[1];
                    requirements[0] = new Requirement(Skill.DEFENCE, level);
                    try {
                        ((Map<ResourceLocation, Requirement[]>) config.getClass().getDeclaredField("skillLocks").get(config)).put(entry.getValue().getRegistryName(), requirements);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
