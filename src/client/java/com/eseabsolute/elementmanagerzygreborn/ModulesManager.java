package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.listener.UpdateListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

public final class ModulesManager implements UpdateListener {
    private static final EleManager eleManager = EleManager.INSTANCE;
    private static final MinecraftClient client = EleManager.mc;
    private static ClientPlayerEntity player;

    public final SettingsManager settingsManager = new SettingsManager();
    public final DepositManager depositManager = new DepositManager();

    private boolean toggleKeyPressed = true;
    @Override
    public void onUpdate() {
        player = client.player;
        GameOptions options = client.options;
        if (player == null || options == null) return;
        KeyBinding toggleKey = eleManager.getToggleKey();
        if (toggleKey.isPressed()) {
            if (toggleKeyPressed) return;
            if (options.sneakKey.isPressed()) {
                onModuleSet();
            } else {
                onToggle();
            }
            toggleKeyPressed = true;
        } else {
            if (toggleKeyPressed) { toggleKeyPressed = false; }
        }
        if (isSetting) {
            settingsManager.setActionbarMsg(depositManager.processMsg(settingsManager.currentSelected()));
        }
    }

    private boolean isSetting = false;
    public void enableModule(KeyBinding toggleKey) {
        depositManager.onEnable();
        depositManager.loadCategoryStatus();
        if (player != null) {
            player.sendMessage(Text.translatable("text.elemanager.enable",
                    toggleKey.getBoundKeyLocalizedText().getString().toUpperCase()
            ));
        }
    }
    private void onToggle() {
        KeyBinding toggleKey = eleManager.getToggleKey();
        if (isSetting) {
            depositManager.onToggle(settingsManager.currentSelected());
            return;
        }
        if (!depositManager.ifEnabled()) {
            enableModule(toggleKey);
        } else {
            depositManager.onDisable();
            player.sendMessage(Text.translatable("text.elemanager.disable"));
        }
    }
    private void onModuleSet() {
        isSetting ^= true;
        eleManager.toggleHotbarScrollLock(isSetting);
        if (isSetting) {
            KeyBinding toggleKey = eleManager.getToggleKey();
            settingsManager.onEnable();
            player.sendMessage(Text.translatable("text.elemanager.entersettings"));
            player.sendMessage(Text.translatable("text.elemanager.guide.line1",
                    toggleKey.getBoundKeyLocalizedText().getString().toUpperCase()
            ));
            player.sendMessage(Text.translatable("text.elemanager.guide.line2"));
            player.sendMessage(Text.translatable("text.elemanager.guide.line3"));
            player.sendMessage(Text.translatable("text.elemanager.guide.line4"));
            player.sendMessage(Text.translatable("text.elemanager.guide.line5",
                    toggleKey.getBoundKeyLocalizedText().getString().toUpperCase()
            ));
        } else {
            settingsManager.onDisable();
            depositManager.saveCategoryStatus();
            player.sendMessage(Text.translatable("text.elemanager.quitsettings"));
        }
    }
}
