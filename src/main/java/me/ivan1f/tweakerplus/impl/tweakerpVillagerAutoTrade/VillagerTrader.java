package me.ivan1f.tweakerplus.impl.tweakerpVillagerAutoTrade;

import fi.dy.masa.itemscroller.util.InventoryUtils;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.InfoUtils;
import me.ivan1f.tweakerplus.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.container.MerchantContainer;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;

import java.util.List;
import java.util.stream.Collectors;

public class VillagerTrader {
    public static int selectedIndex = 0;
    private final MerchantScreen screen;
    private final MerchantContainer container;

    public VillagerTrader(MerchantScreen screen) {
        this.screen = screen;
        this.container = screen.getContainer();
    }

    @SuppressWarnings("unused")
    public static boolean tradeEverything(KeyAction keyAction, IKeybind iKeybind) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof MerchantScreen) {
            VillagerTrader trader = new VillagerTrader((MerchantScreen) currentScreen);
            trader.tradeEverything();
        } else {
            InfoUtils.printActionbarMessage("tweakerplus.config.tweakpTradeEverything.not_merchant_screen");
        }
        return true;
    }

    private boolean isResultSatisfied(ItemStack stack) {
        return this.container.getSlot(2).hasStack() &&
                InventoryUtils.areStacksEqual(this.container.getSlot(2).getStack(), stack);
    }

    public void tradeEverything() {
        TradeOffer offer = this.container.getRecipes().get(selectedIndex);
        if (offer.isDisabled()) return;
        ItemStack sellItem = offer.getSellItem();
        this.prepareBuySlots();
        for (int failSafe = 1024; failSafe >= 0 && isResultSatisfied(sellItem); --failSafe) {
            this.processOutputSlot();
            this.prepareBuySlots();
        }
    }

    public void processOutputSlot() {
        InventoryUtils.dropStacksUntilEmpty(this.screen, 2);
    }

    private void prepareBuySlots() {
        TradeOffer offer = this.container.getRecipes().get(selectedIndex);
        if (offer.isDisabled()) return;
        ItemStack firstBuyItem = offer.getAdjustedFirstBuyItem();
        ItemStack secondBuyItem = offer.getSecondBuyItem();
        ItemStack sellItem = offer.getSellItem();
        clearVillagerTradingSlots();
        pickItemsAndPutToVillagerTradingSlot(firstBuyItem, 0);
        pickItemsAndPutToVillagerTradingSlot(secondBuyItem, 1);
    }

    private void clearVillagerTradingSlots() {
        InventoryUtils.leftClickSlot(this.screen, 0);
        InventoryUtils.tryClearCursor(this.screen, MinecraftClient.getInstance());
        InventoryUtils.leftClickSlot(this.screen, 1);
        InventoryUtils.tryClearCursor(this.screen, MinecraftClient.getInstance());
    }

    public void pickItemsAndPutToVillagerTradingSlot(ItemStack stack, int targetSlot) {
        List<Slot> playerInvSlots = this.container.slots.stream().filter(slot -> slot.inventory instanceof PlayerInventory).collect(Collectors.toList());
        InventoryUtil.pickItemsInCursor(this.screen, playerInvSlots, stack);
        InventoryUtils.leftClickSlot(this.screen, targetSlot);
    }
}
