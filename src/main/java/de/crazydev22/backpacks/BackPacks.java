package de.crazydev22.backpacks;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BackPacks extends JavaPlugin {
    private final NamespacedKey backPackKey = new NamespacedKey(this, "uuid");
    private final NamespacedKey recipieKey = new NamespacedKey(this, "recipe");
    private BackPackManager backPackManager;
    private CraftingManager craftingManager;
    private int customModel = 0;
    private int rows = 6;
    private Component title = Component.text("Backpack");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        customModel = getConfig().getInt("custom-model", 0);
        rows = getConfig().getInt("rows", 6);
        if (rows < 1)
            rows = 1;
        if (rows > 6)
            rows = 6;
        title = MiniMessage.miniMessage().deserialize(getConfig().getString("name", "Backpack"));

        craftingManager = new CraftingManager(this);
        backPackManager = new BackPackManager(this);
    }

    @Override
    public void onDisable() {
        if (backPackManager != null) {
            backPackManager.saveAll();
        }
    }
}
