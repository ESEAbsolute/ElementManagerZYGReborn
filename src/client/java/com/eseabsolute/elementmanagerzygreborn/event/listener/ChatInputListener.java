package com.eseabsolute.elementmanagerzygreborn.event.listener;

import com.eseabsolute.elementmanagerzygreborn.event.CancellableEvent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EventListener;

public interface ChatInputListener extends EventListener {
    void onReceivedMessage(ChatInputEvent event);

    class ChatInputEvent extends CancellableEvent<ChatInputListener> {
        private Text component;
        public ChatInputEvent(Text text) {
            component = text;
        }
        public Text getComponent() { return component; }
        @Override
        public void fire(ArrayList<ChatInputListener> listeners) {
            for(ChatInputListener listener : listeners) {
                listener.onReceivedMessage(this);
                if(isCancelled()) { break; }
            }
        }
        @Override
        public Class<ChatInputListener> getListenerType() { return ChatInputListener.class; }
    }
}
