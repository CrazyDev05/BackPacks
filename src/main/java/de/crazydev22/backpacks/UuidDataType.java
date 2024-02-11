package de.crazydev22.backpacks;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UuidDataType implements PersistentDataType<String, UUID> {
	public static final UuidDataType TYPE = new UuidDataType();

	@Override
	public @NotNull Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public @NotNull Class<UUID> getComplexType() {
		return UUID.class;
	}

	@Override
	public @NotNull String toPrimitive(@NotNull UUID uuid, @NotNull PersistentDataAdapterContext context) {
		return uuid.toString();
	}

	@Override
	public @NotNull UUID fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext context) {
		return UUID.fromString(s);
	}
}
