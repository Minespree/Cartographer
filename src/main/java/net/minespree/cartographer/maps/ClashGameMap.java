package net.minespree.cartographer.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.util.GameArea;
import net.minespree.wizard.util.ItemBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class ClashGameMap extends GameMap {

    private GameArea border;
    private List<GameArea> disabledAreas = Lists.newArrayList();
    private Map<String, ClashMapTower> castleAreas = Maps.newHashMap();
    private Map<String, List<ClashMapTower>> towerAreas = Maps.newHashMap();
    private Map<String, GameArea> left = Maps.newHashMap(), right = Maps.newHashMap(), spawns = Maps.newHashMap();
    private Map<String, Integer> slots = Maps.newHashMap();
    private Map<String, Pair<Float, Float>> spawnYawPitch = Maps.newHashMap();

    public ClashGameMap(File map) {
        super(map, new ItemBuilder(Material.GOLD_BLOCK));
    }

    @Override
    public void info() {
        super.info();

        infoItem("Border", border);
        infoItem("Disabled Areas", disabledAreas);
        infoItem("Castles", castleAreas, (p, o) -> p.teleport(((ClashMapTower) o).getFront().randomLocation()));
        infoItem("Towers", towerAreas, (p, o) -> p.teleport(((ClashMapTower) o).getFront().randomLocation()));
        infoItem("Left Sides", left);
        infoItem("Right Sides", right);
        infoItem("Spawns", spawns);
        infoItem("Slots", slots);
        infoItem("Yaw & Pitch", spawnYawPitch);
    }

    @Override
    public void save() {
        super.save();

        if(border != null) {
            border.getPos1().save(configuration.createSection("border.pos1"));
            border.getPos2().save(configuration.createSection("border.pos2"));
        }
        ConfigurationSection areas = configuration.createSection("disabledAreas");
        for (int i = 0; i < disabledAreas.size(); i++) {
            GameArea area = disabledAreas.get(i);
            area.getPos1().save(areas.createSection(i + ".pos1"));
            area.getPos2().save(areas.createSection(i + ".pos2"));
        }
        for (String s : slots.keySet()) {
            ConfigurationSection side = configuration.createSection("sides." + s);
            if(left.containsKey(s)) {
                GameArea leftArea = left.get(s);
                leftArea.getPos1().save(side.createSection("left.pos1"));
                leftArea.getPos2().save(side.createSection("left.pos2"));
            }
            if(right.containsKey(s)) {
                GameArea rightArea = left.get(s);
                rightArea.getPos1().save(side.createSection("right.pos1"));
                rightArea.getPos2().save(side.createSection("right.pos2"));
            }
            if(castleAreas.containsKey(s)) {
                ClashMapTower castle = castleAreas.get(s);
                if(castle.getTower() != null) {
                    castle.getTower().getPos1().save(side.createSection("castle.tower.pos1"));
                    castle.getTower().getPos1().save(side.createSection("castle.tower.pos2"));
                }
                if(castle.getFront() != null) {
                    castle.getFront().getPos1().save(side.createSection("castle.front.pos1"));
                    castle.getFront().getPos1().save(side.createSection("castle.front.pos2"));
                }
            }
            if(towerAreas.containsKey(s)) {
                ConfigurationSection towerSection = side.createSection("towers");
                List<ClashMapTower> towerAreas = this.towerAreas.get(s);
                for (ClashMapTower tower : towerAreas) {
                    if (tower.getTower() != null) {
                        tower.getTower().getPos1().save(towerSection.createSection(tower.getId() + ".tower.pos1"));
                        tower.getTower().getPos1().save(towerSection.createSection(tower.getId() + ".tower.pos2"));
                    }
                    if (tower.getFront() != null) {
                        tower.getFront().getPos1().save(towerSection.createSection(tower.getId() + ".front.pos1"));
                        tower.getFront().getPos1().save(towerSection.createSection(tower.getId() + ".front.pos2"));
                    }
                }
            }
            if(spawns.containsKey(s)) {
                GameArea spawnArea = spawns.get(s);
                spawnArea.getPos1().save(side.createSection("spawn.pos1"));
                spawnArea.getPos2().save(side.createSection("spawn.pos2"));
            }
            if(slots.containsKey(s)) {
                configuration.set(s + ".slot", slots.get(s));
            }
            if(spawnYawPitch.containsKey(s)) {
                configuration.set(s + ".yaw", spawnYawPitch.get(s).getLeft());
                configuration.set(s + ".pitch", spawnYawPitch.get(s).getRight());
            }
        }
    }

    @Override
    public void load() {
        super.load();

        disabledAreas = Lists.newArrayList();
        castleAreas = Maps.newHashMap();
        towerAreas = Maps.newHashMap();
        left = Maps.newHashMap();
        right = Maps.newHashMap();
        spawns = Maps.newHashMap();
        slots = Maps.newHashMap();
        spawnYawPitch = Maps.newHashMap();

        if(configuration.contains("border")) {
            border = new GameArea(configuration.getConfigurationSection("border.pos1"), configuration.getConfigurationSection("border.pos2"));
        }
        if(configuration.contains("disabledAreas")) {
            ConfigurationSection areas = configuration.getConfigurationSection("disabledAreas");
            for (String area : areas.getKeys(false)) {
                disabledAreas.add(new GameArea(areas.getConfigurationSection(area + ".pos1"), areas.getConfigurationSection(area + ".pos2")));
            }
        }
        if(configuration.contains("sides")) {
            ConfigurationSection sidesSection = configuration.getConfigurationSection("sides");
            for (String side : sidesSection.getKeys(false)) {
                ConfigurationSection sideSection = sidesSection.getConfigurationSection(side);
                if(sideSection.contains("left")) {
                    left.put(side, new GameArea(sideSection.getConfigurationSection("left.pos1"), sideSection.getConfigurationSection("left.pos2")));
                }
                if(sideSection.contains("right")) {
                    right.put(side, new GameArea(sideSection.getConfigurationSection("right.pos1"), sideSection.getConfigurationSection("right.pos2")));
                }
                if(sideSection.contains("castle")) {
                    ConfigurationSection castle = sideSection.getConfigurationSection("castle");
                    GameArea tower = null, front = null;
                    if(castle.contains("tower")) {
                        tower = new GameArea(castle.getConfigurationSection("tower.pos1"), castle.getConfigurationSection("tower.pos2"));
                    }
                    if(castle.contains("front")) {
                        front = new GameArea(castle.getConfigurationSection("front.pos1"), castle.getConfigurationSection("front.pos2"));
                    }
                    castleAreas.put(side, new ClashMapTower("", tower, front));
                }
                if(sideSection.contains("towers")) {
                    ConfigurationSection towersSection = sideSection.getConfigurationSection("towers");
                    for (String s : towersSection.getKeys(false)) {
                        ConfigurationSection towerSection = towersSection.getConfigurationSection(s);
                        GameArea tower = null, front = null;
                        if(towerSection.contains("tower")) {
                            tower = new GameArea(towerSection.getConfigurationSection("tower.pos1"), towerSection.getConfigurationSection("tower.pos2"));
                        }
                        if(towerSection.contains("front")) {
                            front = new GameArea(towerSection.getConfigurationSection("front.pos1"), towerSection.getConfigurationSection("front.pos2"));
                        }
                        towerAreas.put(side, towerAreas.computeIfAbsent(side, key -> Lists.newArrayList())).add(new ClashMapTower(s, tower, front));
                    }
                }
                if(sideSection.contains("spawn")) {
                    spawns.put(side, new GameArea(sideSection.getConfigurationSection("spawn.pos1"), sideSection.getConfigurationSection("spawn.pos2")));
                }
                if(sideSection.contains("slot")) {
                    slots.put(side, sideSection.getInt("slot"));
                }
                if(sideSection.contains("yaw") && sideSection.contains("pitch")) {
                    spawnYawPitch.put(side, new ImmutablePair<>((float) sideSection.getDouble("yaw"), (float) sideSection.getDouble("pitch")));
                }
            }
        }
    }

    @Data @AllArgsConstructor
    public static class ClashMapTower {

        private final String id;
        private GameArea tower, front;

    }

}
