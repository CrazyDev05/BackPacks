package de.crazydev22.backpacks;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BackPackManager implements Listener {
	private final Map<UUID, Holder> openBackPacks = new HashMap<>();
	private final Queue<Holder> saveQueue = new ArrayDeque<>();
	private final BackPacks plugin;

	BackPackManager(BackPacks plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			int i = 10;
			while (!saveQueue.isEmpty() && i-- > 0) {
				try {
					var holder = saveQueue.poll();
					if (holder != null) {
						holder.save();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, 5);
	}

	public void saveAll() {
		saveQueue.clear();
		for (Holder holder : openBackPacks.values().toArray(Holder[]::new)) {
			try {
				holder.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public UUID newUUID() {
		UUID uuid = UUID.randomUUID();
		while (openBackPacks.containsKey(uuid) || getFile(uuid).exists()) {
			uuid = UUID.randomUUID();
		}
		openBackPacks.put(uuid, load(uuid));
		return uuid;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		var itemStack = event.getItem();
		if (itemStack == null || itemStack.getItemMeta() == null)
			return;
		var itemMeta = itemStack.getItemMeta();
		var uuid = itemMeta.getPersistentDataContainer().get(plugin.getBackPackKey(), UuidDataType.TYPE);
		if (uuid == null)
			return;
		event.setCancelled(true);
		event.getPlayer().openInventory(openBackPacks.computeIfAbsent(uuid, this::load).getInventory());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof Holder holder))
			return;
		saveQueue.remove(holder);
		saveQueue.add(holder);
	}

	@NotNull
	private Holder load(@NotNull UUID uuid) {
		File file = getFile(uuid);
		YamlConfiguration data = new YamlConfiguration();
		if (file.exists()) {
			try {
				data.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		Holder holder = new Holder(uuid);
		Inventory inventory = Bukkit.createInventory(holder, data.getInt("size", plugin.getRows()*9), plugin.getTitle());
		var section = data.getConfigurationSection("items");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				inventory.setItem(Integer.parseInt(key), section.getItemStack(key));
			}
		}

		holder.setInventory(inventory);
		return holder;
	}

	private File getFile(UUID uuid) {
		File dir = new File(plugin.getDataFolder(), "data");
		if (!dir.exists()) dir.mkdirs();
		return new File(dir, uuid +".yml");
	}

	@Data
	private class Holder implements InventoryHolder {
		private final UUID uuid;
		private Inventory inventory = null;

		public void save() throws IOException {
			var contents = inventory.getContents();
			YamlConfiguration data = new YamlConfiguration();
			data.set("size", inventory.getSize());
			for (int i = 0; i < contents.length; i++) {
				data.set("items."+i, contents[i]);
			}

			File dir = new File(plugin.getDataFolder(), "data");
			if (!dir.exists()) dir.mkdirs();
			File file = new File(dir, uuid +".yml");
			data.save(file);

			if (inventory.getViewers().isEmpty())
				openBackPacks.remove(uuid);
		}
	}
}
