package net.jiang.tutorialmod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.jiang.tutorialmod.enchantment.ModEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {


	@Shadow @Nullable private Slot lastClickedSlot;
	@Shadow protected abstract Slot getSlotAt(double x, double y);


	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;getSlotAt(DD)Lnet/minecraft/screen/slot/Slot;"), method = "mouseClicked")
	private void init(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir){
		Slot slot = this.getSlotAt(mouseX, mouseY);
		if(slot!=null) {
			ItemStack itemStack = slot.getStack();
			System.out.println(itemStack);
			System.out.println(itemStack.getEnchantments());
			if (EnchantmentHelper.getLevel(ModEnchantments.SLIPPERY, itemStack) > 0) {
				placeItemInPlayerInventory(this.client.player, itemStack);
			}
		}
	}

	// 在玩家背包的随机位置放置物品
	@Unique
	private static void placeItemInPlayerInventory(PlayerEntity player, ItemStack itemStack) {
		// 获取玩家背包
		PlayerInventory playerInventory = player.getInventory();

		//数量
		int count = itemStack.getCount();

		// 获取玩家背包的所有物品槽位
		DefaultedList<ItemStack> slots = playerInventory.main;

		// 创建一个随机数生成器
		Random random = new Random();

		// 循环直到找到一个空槽位
		int attempts = 0;
		while (attempts < 100) { // 防止无限循环
			// 生成一个随机的槽位索引
			int slotIndex = random.nextInt(slots.size());

			// 获取该槽位的物品堆
			ItemStack slotStack = slots.get(slotIndex);

			// 检查该槽位是否为空
			if (slotStack.isEmpty()) {
				// 如果槽位为空，则将物品放置到该槽位
				itemStack.decrement(count);
				slots.set(slotIndex, itemStack.copy());
				// 可选：通知玩家物品已经放置到背包中
				System.out.println("已将物品放置到背包中");
				return;
			}

			// 尝试下一个槽位
			attempts++;
		}

		// 如果没有找到空槽位，则在控制台输出消息
		System.out.println("无法找到空槽位放置物品");
	}
}