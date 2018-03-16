package net.minespree.cartographer.util;

import net.minespree.wizard.util.Area;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class GameArea extends Area {

    public GameArea(ConfigurationSection pos1, ConfigurationSection pos2) {
        super(pos1, pos2);
    }

    public GameArea(GameLocation centre, double xWidth, double yHeight, double zWidth) {
        this(centre.getX() + xWidth, centre.getX() - xWidth, centre.getY() + yHeight, centre.getY() - yHeight, centre.getZ() + zWidth, centre.getZ() - zWidth);
    }

    public GameArea(GameLocation pos1, GameLocation pos2) {
        super(pos1.getX(), pos2.getX(), pos1.getY(), pos2.getY(), pos1.getZ(), pos2.getZ());
    }

    public GameArea(double x1, double x2, double y1, double y2, double z1, double z2) {
        super(x1, x2, y1, y2, z1, z2);
    }

    public GameLocation getPos1() {
        return new GameLocation(getXMin(), getYMin(), getZMin(), 0.0f, 0.0f);
    }

    public GameLocation getPos2() {
        return new GameLocation(getXMax(), getYMax(), getZMax(), 0.0f, 0.0f);
    }

    public GameLocation centreLocation() {
        return new GameLocation((getXMin() + getXMax()) / 2.0, (getYMin() + getYMax()) / 2.0, (getZMin() + getZMax()) / 2.0, 0, 0);
    }

    public Location randomLocation(float yaw, float pitch) {
        return randomLocation(false, yaw, pitch);
    }

    public Location randomLocation() {
        return randomLocation(false, 0.0F, 0.0F);
    }

    public Location randomLocation(boolean y) {
        return randomLocation(y, 0.0F, 0.0F);
    }

    public Location randomLocation(boolean y, float yaw, float pitch) {
        return randomLocation(Bukkit.getWorld("gameWorld"), y, yaw, pitch);
    }

}
