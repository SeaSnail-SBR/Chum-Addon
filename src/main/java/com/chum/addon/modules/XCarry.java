package com.chum.addon.modules;


import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class XCarry extends Module {
    private boolean ignoreNextClose = false;

    public XCarry(Category category) {
        super(category, "x-carry", "Allows you to store items in your crafting inventory after closing it.");
    }

    // --- ADDED --- \\
    @Override
    public void onActivate() {
        // Tells Meteor to look inside this class for @EventHandler methods
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onDeactivate() {
        // Tells Meteor to stop looking inside this class
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }
// --- END ADDED --- \\


    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        // If Grim sees us interacting with blocks/entities while the server thinks
        // our inventory container is open, it flags immediately.
        if (event.packet instanceof PlayerInteractBlockC2SPacket || event.packet instanceof PlayerInteractEntityC2SPacket) {
            // Force safe state if we try to mine/fight to prevent Grim flags
            return;
        }

        if (event.packet instanceof CloseHandledScreenC2SPacket) {
            if (ignoreNextClose) {
                ignoreNextClose = false;
                return;
            }
            // Cancel the close packet to keep items in crafting matrix
            event.cancel();
        }
    }
}
