package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.UpdateListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Objects;

public final class DepositManager implements UpdateListener {
    private static final int DelayThreshold = 40;

    private static final EleManager eleManager = EleManager.INSTANCE;
    private static final EventManager events = eleManager.getEventManager();
    private static final MinecraftClient client = EleManager.mc;
    private static final ConfigManager configManager = eleManager.getConfigManager();

    private static final String[] categoryTranslateNames = {
            "item.elemanager.panling.metal",
            "item.elemanager.panling.wood",
            "item.elemanager.panling.water",
            "item.elemanager.panling.fire",
            "item.elemanager.panling.earth",
            "item.elemanager.panling.cash.general"
    };
    private static final int[] categoryStatus = {0, 0, 0, 0, 0, 0, 0};
    private static final Map<String, Integer> categoryStatusMap = Map.of(
            "Adjust", 1,
            "Store", 0,
            "Ignore", 1,
            "NormalMode", 0,
            "SCMode", 1
    );
    private static final Item[] categoryItems = {
            Items.EMERALD, Items.BONE, Items.STRING, Items.BLAZE_ROD, Items.MAGMA_CREAM,
            Items.GOLD_NUGGET, Items.GOLD_INGOT, Items.IRON_INGOT
    };
    private static final String[] categoryNBTId = {
            "panling:metal", "panling:wood", "panling:water", "panling:fire", "panling:earth",
            "panling:copper_cash", "panling:gold_ingot", "panling:silver_ticket"
    };

    public final CmdFeedbackFetcher cmdFeedbackFetcher = new CmdFeedbackFetcher();

    public void loadCategoryStatus() {
        categoryStatus[0] = categoryStatusMap.get(configManager.readConfig("Metal", "Store"));
        categoryStatus[1] = categoryStatusMap.get(configManager.readConfig("Wood", "Store"));
        categoryStatus[2] = categoryStatusMap.get(configManager.readConfig("Water", "Store"));
        categoryStatus[3] = categoryStatusMap.get(configManager.readConfig("Fire", "Store"));
        categoryStatus[4] = categoryStatusMap.get(configManager.readConfig("Earth", "Store"));
        categoryStatus[5] = categoryStatusMap.get(configManager.readConfig("CurrentItems", "Store"));
        categoryStatus[6] = categoryStatusMap.get(configManager.readConfig("IntervalMode", "NormalMode"));
    }

    public void saveCategoryStatus() {
        configManager.writeConfig("Metal", (categoryStatus[0] == 0 ? "Store" : "Adjust"));
        configManager.writeConfig("Wood", (categoryStatus[1] == 0 ? "Store" : "Adjust"));
        configManager.writeConfig("Water", (categoryStatus[2] == 0 ? "Store" : "Adjust"));
        configManager.writeConfig("Fire", (categoryStatus[3] == 0 ? "Store" : "Adjust"));
        configManager.writeConfig("Earth", (categoryStatus[4] == 0 ? "Store" : "Adjust"));
        configManager.writeConfig("CurrentItems", (categoryStatus[5] == 0 ? "Store" : "Ignore"));
        configManager.writeConfig("IntervalMode", (categoryStatus[6] == 0 ? "NormalMode" : "SCMode"));
        configManager.saveConfig();
    }

    private int tickRemain = 0;
    private int replyTimer = 0;
    private double tickMultiplier = 0;
    private boolean enabled = false;
    @Override
    public void onUpdate() {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (cmdFeedbackFetcher.receivedCmdFeedback() == 2) {
            // Inventory full -> module auto stop
            player.sendMessage(Text.translatable("text.elemanager.inventoryfull"));
            this.onDisable();
            return;
        }
        if (cmdFeedbackFetcher.receivedCmdFeedback() == 0) {
            // Not received command feedback
            replyTimer++;
            if (replyTimer > 200) {
                // No feedback 10s
                player.sendMessage(Text.translatable("text.elemanager.responseignored"));
                replyTimer = 0;
                cmdFeedbackFetcher.resetStatus();
            } else if (replyTimer == 80) {
                // No feedback 4s
                player.sendMessage(Text.translatable("text.elemanager.responsenotreceived"));
            }
            return;
        }
        if (tickRemain > 0) {
            // In cooldown
            tickRemain--;
            return;
        }
        // stop auto storing when trading, opening a chest...
        if (client.currentScreen instanceof HandledScreen) {
            if (!(client.currentScreen instanceof InventoryScreen)) {
                return;
            }
        }
        int[] itemCount = {0, 0, 0, 0, 0, 0, 0, 0};

        for (int slot = 9; slot < 45; slot++) {
            int adjustedSlot = slot % 36;
            ItemStack stack = client.player.getInventory().getStack(adjustedSlot);
            if (stack.isEmpty() || stack.getNbt() == null) continue;
            Item item = stack.getItem();
            for (int i = 0; i < 8; i++) {
                if (item.equals(categoryItems[i]) &&
                        Objects.equals(stack.getNbt().getString("id"), categoryNBTId[i])) {
                    itemCount[i] += stack.getCount();
                }
            }
        }

        double maxWeight = -1;
        int maxWeightIndex = -1, op = 0, storeAll = 1;
        for (int i = 0; i < 5; i++) {
            double weight = 0;
            int optmp = 0, storeAllTemp = 1;
            if (categoryStatus[i] == 1) {                   // Auto Balance Mode
                if (itemCount[i] > 64) {                    // force store all
                    weight = 99999;
                } else if (itemCount[i] > 10) {             // store 5
                    weight = (itemCount[i] - 10) / 5.0;
                    storeAllTemp = 0;
                } else if (itemCount[i] < 5) {              // take out 5
                    weight = 10 - itemCount[i]; optmp = 1;
                }
            } else {                                        // Store All Mode
                if (itemCount[i] == 0) continue;            // no need to store
                weight = (itemCount[i] + 16) / 64.0;        // store all
            }

            if (weight > maxWeight && weight > 0) {
                maxWeight = weight;
                maxWeightIndex = i;
                op = optmp;
                storeAll = storeAllTemp;
            }
        }
        if (categoryStatus[5] == 0) {                       // store item currents
            double weight = 0;
            for (int i = 5; i < 8; i++) {
                if (itemCount[i] == 0) continue;            // no need to store
                weight = (itemCount[i] + 16) / 64.0;        // store

                if (weight > maxWeight && weight > 0) {
                    maxWeight = weight;
                    maxWeightIndex = i;
                }
            }
        }

        if (maxWeightIndex == -1) return;
        if (maxWeight == 99999) {               // Store All
            player.sendMessage(Text.translatable("text.elemanager.elementstoreall",
                    Text.translatable(categoryTranslateNames[maxWeightIndex])));
            catchTriggerCommandFeedback(player, 2, 4 * maxWeightIndex + 4);
        } else {
            if (op == 1) {                      // Take 5
                catchTriggerCommandFeedback(player, 2, 4 * maxWeightIndex + 1);
            } else if (maxWeightIndex < 5) {    // Store 5 or All depends on mode chosen
                catchTriggerCommandFeedback(player, 2, 4 * maxWeightIndex + 3 + storeAll);
            } else {                            // Store all item current
                catchTriggerCommandFeedback(player, 3, 3 * (maxWeightIndex - 5) + 1);
            }
        }

        ClientPlayNetworkHandler clientPlayNetworkHandler = client.getNetworkHandler();
        int latency = 0;
        if (clientPlayNetworkHandler != null) {
            PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(client.player.getUuid());
            if (playerListEntry != null) {
                latency = playerListEntry.getLatency();
            }
        }

        tickMultiplier = tickMultiplier * 0.75 + Math.max((replyTimer * 20 - latency) / 20, 0) * 0.25;
        int randTick = DelayThreshold + (int) (Math.random() * 10 - 5);
        if (categoryStatus[6] == 1) {
            tickRemain = (int) (randTick * Math.sqrt(tickMultiplier + 1));
            if (tickRemain > 30) {
                player.sendMessage(Text.translatable("text.elemanager.servercommandcastdelay",
                        String.valueOf(tickRemain)));
            }
        } else {
            tickRemain = randTick;
        }
        replyTimer = 0;
    }

    public void catchTriggerCommandFeedback(ClientPlayerEntity player, int cdIndex, int val) {
        cmdFeedbackFetcher.setExpectedFeedback(cdIndex, val);
        player.networkHandler.sendCommand("trigger caidan%d set %d".formatted(cdIndex, val));
    }
    public void onToggle(int index) { categoryStatus[index] ^= 1; }
    public void onEnable() {
        cmdFeedbackFetcher.onEnable();
        configManager.writeConfig("Enabled", "True");
        configManager.saveConfig();
        events.add(UpdateListener.class, this);
        replyTimer = 0;
        enabled = true;
    }
    public void onDisable() {
        events.remove(UpdateListener.class, this);
        cmdFeedbackFetcher.onDisable();
        configManager.writeConfig("Enabled", "False");
        configManager.saveConfig();
        enabled = false;
    }
    public boolean ifEnabled() {
        return enabled;
    }

    public Text processMsg(int index) {
        Text processed = Text.empty();

        if (index == 0) processed = processed.copy().append(Text.of("§r|§b<"));
        else processed = processed.copy().append(Text.of("§r| "));

        for (int i = 0; i < 5; i++) {
            processed = processed.copy().append(Text.translatable(categoryTranslateNames[i])).append("§r:");
            processed = processed.copy().append( ( categoryStatus[i] == 1 ?
                    Text.translatable("text.elemanager.config.adjust") :
                    Text.translatable("text.elemanager.config.store") ) );
            if (index == i + 1) processed = processed.copy().append("§r |§b<");
            else if (index == i) processed = processed.copy().append("§b>§r| ");
            else processed = processed.copy().append("§r | ");
        }

        processed = processed.copy().append(Text.translatable(categoryTranslateNames[5])).append("§r:");
        processed = processed.copy().append( ( categoryStatus[5] == 1 ?
                Text.translatable("text.elemanager.config.ignore") :
                Text.translatable("text.elemanager.config.store") ) );
        if (index == 5) processed = processed.copy().append("§b>§r| ");
        else if (index == 6) processed = processed.copy().append("§r |§b<");
        else processed = processed.copy().append("§r | ");

        processed = processed.copy().append( ( categoryStatus[6] == 1 ?
                Text.translatable("text.elemanager.config.scmode") :
                Text.translatable("text.elemanager.config.normalmode") ) );
        if (index == 6) processed = processed.copy().append("§b>§r|");
        else processed = processed.copy().append("§r |");

        return processed;
    }
}
