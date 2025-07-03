package com.eseabsolute.elementmanagerzygreborn.mixins;

import com.eseabsolute.elementmanagerzygreborn.event.EventManager;
import com.eseabsolute.elementmanagerzygreborn.event.listener.MouseScrollListener.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = {@At("RETURN")}, method = {"onMouseScroll(JDD)V"})
    private void onOnMouseScroll(long l1, double d1, double d2, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(d2);
        EventManager.fire(event);
    }
}