package com.eseabsolute.elementmanagerzygreborn.event;

import com.eseabsolute.elementmanagerzygreborn.EleManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import java.util.*;

public final class EventManager {
    private final EleManager cli;
    private final HashMap < Class <? extends EventListener>, ArrayList <? extends EventListener> >
                        listenerMap = new HashMap<>();

    public EventManager(EleManager cli) { this.cli = cli; }

    public static < L extends EventListener, E extends Event <L> > void fire(E event) {
        EventManager eventManager = EleManager.INSTANCE.getEventManager();
        if (eventManager == null) return;
        eventManager.fireImpl(event);
    }

    private < L extends EventListener, E extends Event <L> > void fireImpl(E event) {
        try {
            Class<L> type = event.getListenerType();
            @SuppressWarnings("unchecked")
            ArrayList<L> listeners = (ArrayList<L>) listenerMap.get(type);
            if (listeners == null || listeners.isEmpty()) return;
            ArrayList<L> listenerscpy = new ArrayList<>(listeners);
            listenerscpy.removeIf(Objects::isNull);
            event.fire(listenerscpy);
        } catch (Throwable e) {
            e.printStackTrace();
            CrashReport report = CrashReport.create(e, "Firing EleManager Event");
            CrashReportSection section = report.addElement("Affected Event");
            section.add("Event class", () -> event.getClass().getName());
            throw new CrashException(report);
        }
    }
    public < L extends EventListener > void add(Class<L> type, L listener) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<L> listeners = (ArrayList<L>) listenerMap.get(type);
            if (listeners == null) {
                listeners = new ArrayList<>(Arrays.asList(listener));
                listenerMap.put(type, listeners);
                return;
            }
            listeners.add(listener);
        } catch (Throwable e) {
            e.printStackTrace();
            CrashReport report = CrashReport.create(e, "Adding EleManager Event Listener");
            CrashReportSection section = report.addElement("Affected Listener");
            section.add("Listener type", () -> type.getName());
            section.add("Listener class", () -> listener.getClass().getName());
            throw new CrashException(report);
        }
    }
    public < L extends EventListener > void remove(Class<L> type, L listener) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<L> listeners = (ArrayList<L>) listenerMap.get(type);
            if (listeners != null) { listeners.remove(listener); }
        } catch (Throwable e) {
            e.printStackTrace();
            CrashReport report = CrashReport.create(e, "Removing EleManager Event Listener");
            CrashReportSection section = report.addElement("Affected Listener");
            section.add("Listener type", () -> type.getName());
            section.add("Listener class", () -> listener.getClass().getName());
            throw new CrashException(report);
        }
    }
}
