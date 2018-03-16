package net.minespree.cartographer.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class GameLocation implements Cloneable {

    private final static String GAME_WORLD = "gameWorld";

    private double x, y, z;
    private float yaw, pitch;

    public GameLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public GameLocation(Location location) {
        this(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public GameLocation(ConfigurationSection section) {
        this(section.getDouble("X"), section.getDouble("Y"), section.getDouble("Z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }

    public void save(ConfigurationSection section) {
        section.set("X", x);
        section.set("Y", y);
        section.set("Z", z);
        section.set("yaw", yaw);
        section.set("pitch", pitch);
    }

    public GameLocation add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public GameLocation subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(GAME_WORLD), x, y, z, yaw, pitch);
    }

    public GameLocation clone() {
        return new GameLocation(x, y, z, yaw, pitch);
    }

    public boolean equals(GameLocation location) {
        return x == location.getX() && y == location.getY() && z == location.getZ() && yaw == location.getYaw() && pitch == location.getPitch();
    }

}
