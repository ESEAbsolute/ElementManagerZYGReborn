package com.eseabsolute.elementmanagerzygreborn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ElementManagerZYGRebornClient implements ClientModInitializer {
    private static boolean initialized;

    @Override
    public void onInitializeClient() {
        if (initialized) throw new RuntimeException("EleMgrInitializer.onInitializeClient() ran twice!");
        EleManager.INSTANCE.onInitializeClient();
        initialized = true;
    }
}
