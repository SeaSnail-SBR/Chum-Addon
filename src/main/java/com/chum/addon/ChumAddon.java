package com.chum.addon;

import com.chum.addon.hud.ClockHud;
import com.chum.addon.commands.CommandExample;
import com.chum.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class ChumAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Chum");
    public static final HudGroup HUD_GROUP = new HudGroup("Chum");


        @Override
        public void onInitialize() {
            LOG.info("Initializing Meteor Addon Template");

            // Modules \\
            Modules.get().add(new AutoCampfire(CATEGORY));
            Modules.get().add(new BetterTracers(CATEGORY));
            Modules.get().add(new WallMiner(CATEGORY));
            Modules.get().add(new WallTrail(CATEGORY));
            Modules.get().add(new XCarry(CATEGORY));
            Modules.get().add(new SlowWalkSneak(CATEGORY));
            Modules.get().add(new BlockDisguise(CATEGORY));


            // Commands \\
            Commands.add(new CommandExample());

            // HUD \\
            Hud.get().register(ClockHud.INFO);
        }

        @Override
        public void onRegisterCategories() {
            Modules.registerCategory(CATEGORY);
        }

        @Override
        public String getPackage() {
            return "com.chum.addon";
        }

        @Override
        public GithubRepo getRepo() {
            return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
        }
    }

