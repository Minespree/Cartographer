package net.minespree.cartographer.maps;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Setter @Getter
public class SkywarsGameMap extends GameMap {

    // Shared data
    private boolean team;
    private Map<Integer, List<GameLocation>> tieredChests = new HashMap<>();
    private GameArea border;
    private GameLocation centre;
    private Map<GameArea, List<GameLocation>> islands = new HashMap<>();
    private Double radius;
    private Map<Integer, List<ItemData>> mapSpecificItems = new HashMap<>();

    // Team
    private Map<ColourData, List<GameLocation>> teamSpawns = new HashMap<>();
    private Map<ColourData, Integer> teamSlots = new HashMap<>();

    // Solo
    private List<GameLocation> soloSpawns = new ArrayList<>();

    public SkywarsGameMap(File map) {
        super(map, new ItemBuilder(Material.GRASS));
    }

    @Override
    public void info() {
        super.info();

        int islandChests = 0;

        for (List<GameLocation> locations : islands.values()) {
            islandChests += locations.size();
        }

        infoItem("Team", team, o -> team = (boolean) o);
        infoItem("Islands", islands);
        infoItem("Island Chests", islandChests);
        infoItem("Normal Chests", tieredChests.getOrDefault(1, Collections.emptyList()));
        infoItem("Inner Chests", tieredChests.getOrDefault(2, Collections.emptyList()));
        infoItem("Lucky Chests", tieredChests.getOrDefault(3, Collections.emptyList()));
        infoItem("Border", border);
        infoItem("Decay Centre", centre);
        infoItem("Decay Radius", radius, o -> radius = (Double) o);
        infoItem("Time", time);
        if(team) {
            infoItem("Team Spawns", teamSpawns);
        } else {
            infoItem("Solo Spawns", soloSpawns);
        }
    }

    @Override
    public void save() {
        super.save();

        configuration.set("teams", team);
        configuration.createSection("chests");
        tieredChests.forEach((id, chests) -> {
            for (int i = 0; i < chests.size(); i++) {
                chests.get(i).save(configuration.createSection("chests." + id + "." + i));
            }
        });
        if(border != null) {
            border.getPos1().save(configuration.createSection("border.pos1"));
            border.getPos2().save(configuration.createSection("border.pos2"));
        }
        if(centre != null)
            centre.save(configuration.createSection("decay.centre"));
        if(team) {
            for (ColourData data : teamSpawns.keySet()) {
                for (int i = 0; i < teamSpawns.get(data).size(); i++) {
                    GameLocation location = teamSpawns.get(data).get(i);
                    location.save(configuration.createSection("teams." + data.name() + ".spawns." + i));
                }
                if(teamSlots != null && teamSlots.containsKey(data)) {
                    configuration.set("teams." + data.name() + ".slot", teamSlots.get(data));
                }
            }
        } else {
            for (int i = 0; i < soloSpawns.size(); i++) {
                GameLocation spawn = soloSpawns.get(i);
                spawn.save(configuration.createSection("spawns." + i));
            }
        }
        if(radius != null)
            configuration.set("radius", radius);
        int i = 0;
        for (GameArea area : islands.keySet()) {
            area.getPos1().save(configuration.createSection("islands." + i + ".pos1"));
            area.getPos2().save(configuration.createSection("islands." + i + ".pos2"));
            ConfigurationSection section = configuration.createSection("islands." + i + ".chests");
            for (int j = 0; j < islands.get(area).size(); j++) {
                GameLocation location = islands.get(area).get(j);
                location.save(section.createSection(j + ""));
            }
            i++;
        }
    }

    @Override
    public void load() {
        super.load();

        if(configuration.contains("teams"))
            team = configuration.getBoolean("teams");
        tieredChests = new HashMap<>();
        if(configuration.contains("chests")) {
            ConfigurationSection chests = configuration.getConfigurationSection("chests");
            for (String tierKey : chests.getKeys(false)) {
                ConfigurationSection tiers = chests.getConfigurationSection(tierKey);
                for (String id : tiers.getKeys(false)) {
                    int tier = Integer.parseInt(tierKey);
                    tieredChests.computeIfAbsent(tier, (i) -> new ArrayList<>());
                    tieredChests.get(tier).add(new GameLocation(tiers.getConfigurationSection(id)));
                }
            }
        }
        if(configuration.contains("border.pos1") && configuration.contains("border.pos2")) {
            border = new GameArea(configuration.getConfigurationSection("border.pos1"),
                    configuration.getConfigurationSection("border.pos2"));
        }
        if(configuration.contains("decay.centre"))
            centre = new GameLocation(configuration.getConfigurationSection("decay.centre"));
        if(team && configuration.contains("teams")) {
            teamSpawns = new HashMap<>();
            teamSlots = new HashMap<>();
            if(configuration.contains("teams")) {
                ConfigurationSection spawns = configuration.getConfigurationSection("teams");
                for (String team : spawns.getKeys(false)) {
                    ColourData data = ColourData.valueOf(team.toUpperCase());
                    List<GameLocation> spawnLocations = spawns.getConfigurationSection(team + ".spawns")
                            .getKeys(false).stream()
                            .map((spawn) -> new GameLocation(spawns.getConfigurationSection(team + ".spawns." + spawn)))
                            .collect(Collectors.toList());
                    if(spawns.contains(team + ".slot"))
                        teamSlots.put(data, spawns.getInt(team + ".slot"));
                    teamSpawns.put(data, spawnLocations);
                }
            }
        } else {
            soloSpawns = new ArrayList<>();
            if(configuration.contains("spawns")) {
                ConfigurationSection spawns = configuration.getConfigurationSection("spawns");
                soloSpawns = spawns.getKeys(false).stream()
                        .map((spawn) -> new GameLocation(spawns.getConfigurationSection(spawn)))
                        .collect(Collectors.toList());
            }
        }
        if(configuration.contains("radius")) {
            radius = configuration.getDouble("radius");
        }
        islands = new HashMap<>();
        if(configuration.contains("islands")) {
            for (String island : configuration.getConfigurationSection("islands").getKeys(false)) {
                ConfigurationSection islandSection = configuration.getConfigurationSection("islands." + island);
                GameArea area = new GameArea(new GameLocation(islandSection.getConfigurationSection("pos1")),
                        new GameLocation(islandSection.getConfigurationSection("pos2")));
                List<GameLocation> chests = new ArrayList<>();
                if(islandSection.contains("chests")) {
                    for (String chest : islandSection.getConfigurationSection("chests").getKeys(false)) {
                        chests.add(new GameLocation(islandSection.getConfigurationSection("chests." + chest)));
                    }
                }
                islands.put(area, chests);
            }
        }
        mapSpecificItems = new HashMap<>();
        if(configuration.contains("items")) {
            ConfigurationSection tiers = configuration.getConfigurationSection("items");
            for (String tierStr : tiers.getKeys(false)) {
                int tier = Integer.parseInt(tierStr);
                List<ItemData> items = new ArrayList<>();
                for (String item : tiers.getConfigurationSection(tierStr).getKeys(false)) {
                    items.add(loadItem(item, configuration.getConfigurationSection("items." + tier + "." + item)));
                }
                mapSpecificItems.put(tier, items);
            }
        }
    }

    public ItemData loadItem(String id, ConfigurationSection section) {
        ItemBuilder builder = new ItemBuilder(section);
        return new ItemData(id, section.getString("itemType"), builder, section.contains("min") ? section.getInt("min") : 1,
                section.contains("max") ? section.getInt("max") : 1, section.getDouble("weight"),
                section.contains("guaranteed") ? section.getStringList("guaranteed") : new ArrayList<>(),
                section.contains("overrides") ? section.getStringList("overrides") : new ArrayList<>());
    }


    @Data @Getter @Setter
    public static class ItemData {

        private final String id;
        private final String itemType;
        private final ItemBuilder builder;
        private final int min;
        private final int max;
        private final double weight;
        private final List<String> guaranteed;
        private final List<String> overrides;

    }

}
