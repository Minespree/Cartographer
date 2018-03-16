package net.minespree.cartographer.maps;

import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.wizard.util.ItemBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class BlockwarsGameMap extends GameMap {

    private GameArea border, wall;
    private Map<ColourData, GameArea> teamSpawns = new HashMap<>();
    private Map<ColourData, GameLocation> teamCores = new HashMap<>();
    private Map<ColourData, Pair<Float, Float>> teamSpawnDirection = new HashMap<>();
    private Map<ColourData, Integer> teamSlots = new HashMap<>();
    private List<GameLocation> neutralCores = new ArrayList<>();
    private List<ItemStack> wallItems = new ArrayList<>();
    private List<GameArea> disabledAreas = new ArrayList<>();
    private Integer buildY;

    public BlockwarsGameMap(File map) {
        super(map, new ItemBuilder(Material.STAINED_CLAY).durability((short) 14));
    }

    @Override
    public void save() {
        super.save();

        if(border != null) {
            border.getPos1().save(configuration.createSection("borderPos1"));
            border.getPos2().save(configuration.createSection("borderPos2"));
        }
        if(wall != null) {
            wall.getPos1().save(configuration.createSection("wallPos1"));
            wall.getPos2().save(configuration.createSection("wallPos2"));
        }
        if(buildY != null)
            configuration.set("buildY", buildY);
        for (ColourData data : teamCores.keySet()) {
            ConfigurationSection section = configuration.isConfigurationSection("teams." + data.name()) ?
                    configuration.getConfigurationSection("teams." + data.name()) : configuration.createSection("teams." + data.name());
            teamCores.get(data).save(section.createSection("core"));
            if(teamSpawnDirection.containsKey(data)) {
                Pair<Float, Float> direction = teamSpawnDirection.get(data);
                configuration.set("teams." + data.name() + ".spawnyaw", direction.getLeft());
                configuration.set("teams." + data.name() + ".spawnpitch", direction.getRight());
            }
            if(teamSlots.containsKey(data)) {
                configuration.set("teams." + data.name() + ".slot", teamSlots.get(data));
            }
            if(teamSpawns.containsKey(data)) {
                GameArea area = teamSpawns.get(data);
                area.getPos1().save(section.createSection("spawnpos1"));
                area.getPos2().save(section.createSection("spawnpos2"));
            }
        }
        if(!neutralCores.isEmpty()) {
            ConfigurationSection section = configuration.createSection("neutralcores");
            for (int i = 0; i < neutralCores.size(); i++) {
                GameLocation neutralCore = neutralCores.get(i);
                neutralCore.save(section.createSection(i + ""));
            }
        }
        if(!wallItems.isEmpty()) {
            ConfigurationSection section = configuration.createSection("wallItems");
            for (ItemStack item : wallItems) {
                section.set("" + item.getTypeId(), (int) item.getData().getData());
            }
        }
    }

    @Override
    public void load() {
        super.load();

        if(configuration.contains("borderPos1") && configuration.contains("borderPos2")) {
            border = new GameArea(configuration.getConfigurationSection("borderPos1"),
                    configuration.getConfigurationSection("borderPos2"));
        }
        if(configuration.contains("wallPos1") && configuration.contains("wallPos2")) {
            wall = new GameArea(configuration.getConfigurationSection("wallPos1"),
                    configuration.getConfigurationSection("wallPos2"));
        }
        if(configuration.contains("buildY"))
            buildY = configuration.getInt("buildY");
        teamSpawns = new HashMap<>();
        teamCores = new HashMap<>();
        teamSpawnDirection = new HashMap<>();
        if(configuration.contains("teams")) {
            ConfigurationSection teams = configuration.getConfigurationSection("teams");
            for (String team : teams.getKeys(false)) {
                ColourData data = ColourData.valueOf(team.toUpperCase());
                if(teams.contains(team + ".spawnpos1") && teams.contains(team + ".spawnpos2")) {
                    GameArea area = new GameArea(teams.getConfigurationSection(team + ".spawnpos1"),
                            teams.getConfigurationSection(team + ".spawnpos2"));
                    teamSpawns.put(data, area);
                }
                if(teams.contains(team + ".core")) {
                    GameLocation core = new GameLocation(teams.getConfigurationSection(team + ".core"));
                    teamCores.put(data, core);
                }
                if(teams.contains(team + ".spawnyaw") && teams.contains(team + ".spawnpitch")) {
                    float spawnYaw = (float) teams.getDouble(team + ".spawnyaw");
                    float spawnPitch = (float) teams.getDouble(team + ".spawnpitch");
                    teamSpawnDirection.put(data, new ImmutablePair<>(spawnYaw, spawnPitch));
                }
                if(teams.contains(team + ".slot")) {
                    teamSlots.put(data, teams.getInt(team + ".slot"));
                }
            }
        }
        neutralCores = new ArrayList<>();
        if(configuration.contains("neutralcores")) {
            ConfigurationSection cores = configuration.getConfigurationSection("neutralcores");
            for (String s : cores.getKeys(false)) {
                neutralCores.add(new GameLocation(cores.getConfigurationSection(s)));
            }
        }
        if(configuration.contains("wallItems")) {
            ConfigurationSection items = configuration.getConfigurationSection("wallItems");
            for (String id : items.getKeys(false)) {
                wallItems.add(new ItemStack(Material.getMaterial(Integer.parseInt(id)), 1,
                        (short) Integer.parseInt(items.getString(id))));
            }
        }
        disabledAreas = new ArrayList<>();
        if(configuration.contains("disabledareas")) {
            ConfigurationSection areas = configuration.getConfigurationSection("disabledareas");
            for (String s : areas.getKeys(false)) {
                GameArea area = new GameArea(areas.getConfigurationSection(s + ".pos1"),
                        areas.getConfigurationSection(s + ".pos2"));
                disabledAreas.add(area);
            }
        }
    }

    @Override
    public void info() {
        super.info();

        infoItem("Border", border);
        infoItem("Wall", wall);
        infoItem("Team Spawns", teamSpawns);
        infoItem("Team Cores", teamCores);
        infoItem("Neutral Cores", neutralCores);
        infoItem("Wall Items", wallItems);
        infoItem("Disabled Areas", disabledAreas);
        infoItem("Build Y", buildY, o -> buildY = (Integer) o);
    }
}
