package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.UpdateListener;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;


public enum EleManager {
    INSTANCE;
    public static MinecraftClient mc;
    private EventManager eventManager;
    private ConfigManager configManager;
    private KeyBinding toggleKey;

    private boolean hotbarScrollLock = false;

    public void onInitializeClient() {
        System.out.println("Starting Element Manager for ZYG...");
        mc = MinecraftClient.getInstance();
        eventManager = new EventManager(this);
        configManager = new ConfigManager();

        toggleKey = new KeyBinding(Text.translatable("key.elemanager.togglekey").getString(), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, Text.translatable("category.elemanager.elemanager").getString());
        KeyBindingHelper.registerKeyBinding(toggleKey);

        ModulesManager modulesManager = new ModulesManager();
        eventManager.add(UpdateListener.class, modulesManager);
        if (Objects.equals(configManager.readConfig("Enabled", "False"), "True") ||
                Objects.equals(configManager.readConfig("Enabled", "False"), "true")) {
            modulesManager.enableModule(toggleKey);
        }
    }

    public EventManager getEventManager() { return eventManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public KeyBinding getToggleKey() { return toggleKey; }
    public void toggleHotbarScrollLock(boolean stat) {
        hotbarScrollLock = stat;
    }
    public boolean hotbarLocked() { return hotbarScrollLock; }
}
