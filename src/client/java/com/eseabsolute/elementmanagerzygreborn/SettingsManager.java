package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.MouseScrollListener;
import com.eseabsolute.elementmanagerzygreborn.event.listener.UpdateListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public final class SettingsManager implements MouseScrollListener, UpdateListener {
    private static final EleManager eleManager = EleManager.INSTANCE;
    private static final EventManager events = eleManager.getEventManager();
    private static final MinecraftClient client = EleManager.mc;

    private int toggleIndex = 0;

    @Override
    public void onMouseScroll(double amount) {
        if (amount > 0) {
            toggleIndex--; // scrolling down
        } else if (amount < 0) {
            toggleIndex++; // scrolling up
        }
        toggleIndex = (toggleIndex + 7) % 7;
    }

    public int currentSelected() { return toggleIndex; }

    private static Text actionbarMsg;

    @Override
    public void onUpdate() {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (actionbarMsg == null) return;
        player.sendMessage(actionbarMsg, true);
    }

    public void onEnable() {
        events.add(MouseScrollListener.class, this);
        events.add(UpdateListener.class, this);
    }
    public void onDisable() {
        events.remove(MouseScrollListener.class, this);
        events.remove(UpdateListener.class, this);
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        player.sendMessage(Text.empty(), true);
    }
    public void setActionbarMsg(Text processed) { actionbarMsg = processed; }
}
