package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.UpdateListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;


public final class SettingsDisplay implements UpdateListener {
    private static final EleManager eleManager = EleManager.INSTANCE;
    private static final EventManager events = eleManager.getEventManager();
    private static final MinecraftClient client = EleManager.mc;

    private static String actionbarMsg;

    @Override
    public void onUpdate() {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (actionbarMsg == null) return;
        player.sendMessage(Text.of(actionbarMsg), true);
    }
    public void setActionbarMsg(String processed) { actionbarMsg = processed; }
    public void onEnable() { events.add(UpdateListener.class, this); }
    public void onDisable() { events.remove(UpdateListener.class, this); }
}
