package net.minespree.cartographer.maps;

import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class DoubletroubleGameMap extends GameMap {
    
    private Map<ColourData, GameLocation> teamSpawns = new HashMap<>();
    private Map<ColourData, Integer> teamSlots = new HashMap<>();

    public DoubletroubleGameMap(File file) {
        super(file, new ItemBuilder(Material.SADDLE));
    }

    @Override
    public void load() {
        super.load();

        teamSpawns = new HashMap<>();
        teamSlots = new HashMap<>();

        if (configuration.contains("teams")) {
            ConfigurationSection teams = configuration.getConfigurationSection("teams");

            for (String team : teams.getKeys(false)) {
                ColourData data = ColourData.valueOf(team.toUpperCase());
                GameLocation gameLocation = new GameLocation(teams.getConfigurationSection(team + ".spawn"));

                teamSpawns.put(data, gameLocation);

                if (teams.contains(data + ".slot")) {
                    teamSlots.put(data, teams.getInt(data + ".slot"));
                }
            }
        }
    }

    @Override
    public void info() {
        super.info();
        
        infoItem("Spawns", teamSpawns);
    }

    @Override
    public void save() {
        super.save();

        for (ColourData data : teamSpawns.keySet()) {
            GameLocation gameLocation = teamSpawns.get(data);
            gameLocation.save(configuration.createSection("teams." + data.name() + ".spawn"));
        }

        for (ColourData data : teamSlots.keySet()) {
            int i = teamSlots.get(data);
            configuration.set("teams." + data.name() + ".slot", i);
        }

        saveConfig();
    }
}
