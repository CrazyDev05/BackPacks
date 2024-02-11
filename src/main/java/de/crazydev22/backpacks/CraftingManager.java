package de.crazydev22.backpacks;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.UUID;

public class CraftingManager implements Listener {
	private final BackPacks plugin;

	CraftingManager(BackPacks plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		registerRecipe();
	}

	private void registerRecipe() {
		var backPack = new ItemStack(Material.CHEST);
		var itemMeta = backPack.getItemMeta();
		if (itemMeta != null) {
			itemMeta.displayName(plugin.getTitle());
			if (plugin.getCustomModel() > 0)
				itemMeta.setCustomModelData(plugin.getCustomModel());
		}
		backPack.setItemMeta(itemMeta);

		var recipe = new ShapedRecipe(plugin.getRecipieKey(), backPack);
		recipe.shape("www", "wcw", "www");
		recipe.setIngredient('w', new RecipeChoice.MaterialChoice(Tag.WOOL));
		recipe.setIngredient('c', Material.CHEST);
		Bukkit.addRecipe(recipe);
	}

	@EventHandler
	public void onCraft(PrepareItemCraftEvent event) {
		if (!(event.getRecipe() instanceof ShapedRecipe recipe))
			return;
		if (!recipe.getKey().equals(plugin.getRecipieKey()))
			return;
		var inv = event.getInventory();
		var itemStack = inv.getResult();
		var itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			var pdc = itemMeta.getPersistentDataContainer();
			if (pdc.has(plugin.getBackPackKey(), UuidDataType.TYPE))
				return;
			var source = inv.getMatrix()[4];
			var sourceMeta = source.getItemMeta();
			UUID uuid = null;
			if (sourceMeta != null) {
				var spdc = sourceMeta.getPersistentDataContainer();
				uuid = spdc.get(plugin.getBackPackKey(), UuidDataType.TYPE);
				if (uuid != null)
					itemStack.setAmount(2);
			}
			pdc.set(plugin.getBackPackKey(), UuidDataType.TYPE, uuid == null ?
					plugin.getBackPackManager().newUUID() : uuid);
		}
		itemStack.setItemMeta(itemMeta);
		inv.setResult(itemStack);
	}
}
