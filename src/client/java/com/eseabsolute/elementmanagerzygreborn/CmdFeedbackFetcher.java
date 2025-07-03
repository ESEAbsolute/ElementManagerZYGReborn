package com.eseabsolute.elementmanagerzygreborn;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.ChatInputListener;
import net.minecraft.text.*;

import java.util.ArrayList;
import java.util.List;

public final class CmdFeedbackFetcher implements ChatInputListener {
    private static final EleManager eleManager = EleManager.INSTANCE;
    private static final EventManager events = eleManager.getEventManager();
    private static int caidanIndexExpected = -1, valueExpected = -1;
    public int received = 1;
    
    @Override
    public void onReceivedMessage(ChatInputEvent event) {
        Text msg = event.getComponent();
        String str = msg.getString();

        if (received != 0 || caidanIndexExpected == -1 || valueExpected == -1) { return; }

        if (msg.toString().contains("你的背包空间不足,请先清理背包")) { received = 2; return; }

        final boolean doFetchFeedback = false;
        if (doFetchFeedback) {
            List<TranslatableTextContent> translatableTextContents = getTranslatableTextContent(msg);
            for (TranslatableTextContent content : translatableTextContents) {
                String key = content.getKey();
                if (key.equals("commands.trigger.simple.success") ||
                        key.equals("commands.trigger.add.success") ||
                        key.equals("commands.trigger.set.success") ||
                        key.equals("arguments.objective.notFound") ||
                        key.equals("commands.trigger.failed.unprimed") ||
                        key.equals("command.unknown.command")) {
                    received = 1;
                    return;
                }
            }
        } else {
            received = 1;
        }
    }

    private List<TranslatableTextContent> getTranslatableTextContent(Text text) {
        return getTranslatableTextContentImpl(text, 0);
    }

    private List<TranslatableTextContent> getTranslatableTextContentImpl(Text text, int depth) {
        System.out.println(text.toString());

        List<TranslatableTextContent> translatableTextContents = new ArrayList<>();
        if (text instanceof MutableText mutableText) {
            if (mutableText.getContent() instanceof TranslatableTextContent content) {
                translatableTextContents.add(content);
            }
        }
        for (Text sibling : text.getSiblings()) {
            translatableTextContents.addAll(getTranslatableTextContentImpl(sibling, depth + 1));
        }
        return translatableTextContents;
    }

    public void setExpectedFeedback(int caidanIndex, int value) {
        received = 0;
        caidanIndexExpected = caidanIndex;
        valueExpected = value;
    }
    public void resetStatus() {
        caidanIndexExpected = -1;
        valueExpected = -1;
        received = 1;
    }
    public int receivedCmdFeedback() {
        if (received == 0) return 0;    // expecting but not received
        int tmp = received;
        resetStatus();
        return tmp;
    }
    public void onEnable() {
        resetStatus();
        events.add(ChatInputListener.class, this);
    }
    public void onDisable() {
        resetStatus();
        events.remove(ChatInputListener.class, this);
    }
}
