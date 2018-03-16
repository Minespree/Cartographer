package net.minespree.cartographer.maps;

import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;

import java.io.File;

@Getter @Setter
public class ThimbleGameMap extends GameMap {

    private GameLocation jumpLocation, waitingLocation;
    private GameArea waterArea;

    public ThimbleGameMap(File map) {
        super(map, new ItemBuilder(Material.WATER_BUCKET));
    }

    @Override
    public void save() {
        super.save();

        if(jumpLocation != null)
            jumpLocation.save(configuration.createSection("jumpLocation"));
        if(waitingLocation != null)
            waitingLocation.save(configuration.createSection("waitingLocation"));
        if(waterArea != null) {
            waterArea.getPos1().save(configuration.createSection("waterArea.pos1"));
            waterArea.getPos2().save(configuration.createSection("waterArea.pos2"));
        }
    }

    @Override
    public void load() {
        super.load();

        if(configuration.contains("jumpLocation"))
            jumpLocation = new GameLocation(configuration.getConfigurationSection("jumpLocation"));
        if(configuration.contains("waitingLocation"))
            waitingLocation = new GameLocation(configuration.getConfigurationSection("waitingLocation"));
        if(configuration.contains("waterArea.pos1") && configuration.contains("waterArea.pos2")) {
            waterArea = new GameArea(configuration.getConfigurationSection("waterArea.pos1"),
                    configuration.getConfigurationSection("waterArea.pos2"));
        }
    }

    @Override
    public void info() {
        super.info();

        infoItem("Jump Location", jumpLocation);
        infoItem("Waiting Location", waitingLocation);
        infoItem("Water Area", waterArea);
    }
}
