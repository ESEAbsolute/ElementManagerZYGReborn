package com.eseabsolute.elementmanagerzygreborn.event.listener;

import com.eseabsolute.elementmanagerzygreborn.event.Event;

import java.util.ArrayList;
import java.util.EventListener;

public interface UpdateListener extends EventListener {
    void onUpdate();
    class UpdateEvent extends Event<UpdateListener> {
        public static final UpdateEvent INSTANCE = new UpdateEvent();
        @Override
        public void fire(ArrayList<UpdateListener> listeners) {
            for(UpdateListener listener : listeners) { listener.onUpdate(); }
        }
        @Override
        public Class<UpdateListener> getListenerType() { return UpdateListener.class; }
    }
}
