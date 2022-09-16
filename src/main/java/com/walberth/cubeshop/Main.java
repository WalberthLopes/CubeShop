package com.walberth.cubeshop;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getCommand("shopkey").setExecutor(new ShopKeyCommand(this));
    }
}
