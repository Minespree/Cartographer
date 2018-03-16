package net.minespree.cartographer.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.cartographer.util.MapEditorWand;
import net.minespree.wizard.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkywarsMapCommand extends MapCommand {

    private Map<Material, Integer> chestTiers = new HashMap<Material, Integer>() {
        {put(Material.CHEST, 1);}
        {put(Material.TRAPPED_CHEST, 2);}
        {put(Material.ENDER_CHEST, 3);}
    };

    public SkywarsMapCommand() {
        super("skywars", "sw");

        add("team", "<true/false>", (player, map, args) -> {
            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("false") || args[0].equalsIgnoreCase("true")) {
                    SkywarsGameMap gameMap = (SkywarsGameMap) map;
                    gameMap.setTeam(Boolean.parseBoolean(args[2]));
                    if(gameMap.isTeam()) {
                        gameMap.setTeamSpawns(Maps.newHashMap());
                        gameMap.setTeamSlots(Maps.newHashMap());
                    } else {
                        gameMap.setSoloSpawns(Lists.newArrayList());
                    }
                    return true;
                }
            }
            return false;
        });
        add("border", "", (player, map, args) -> {
            if(args.length == 0) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    gameMap.setBorder(new GameArea(pos1, pos2));
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("radius", "<radius>", (player, map, args) -> {
            if(args.length == 1) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                try {
                    gameMap.setRadius(Double.parseDouble(args[0]));
                } catch(Exception e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                return true;
            }
            return false;
        });
        add("centre", "", (player, map, args) -> {
            if(args.length == 0) {
                ((SkywarsGameMap) map).setCentre(new GameLocation(player.getLocation()));
                return true;
            }
            return false;
        });
        add("teamspawn", "<team>", (player, map, args) -> {
            if(args.length == 1) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                if (!gameMap.isTeam()) {
                    player.sendMessage(Chat.RED + "This map isn't a team map.");
                    return false;
                }
                ColourData data;
                try {
                    data = ColourData.valueOf(args[0]);
                } catch (Exception e) {
                    player.sendMessage(LIST_TEAMS.apply(commands.get(0)));
                    return false;
                }
                gameMap.getTeamSpawns().getOrDefault(data, Lists.newArrayList()).add(new GameLocation(player.getLocation()));
                return true;
            }
            return false;
        });
        add("teamslot", "<team> <slot>", (player, map, args) -> {
            if(args.length == 2) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                if(!gameMap.isTeam()) {
                    player.sendMessage(Chat.RED + "This map isn't a team map.");
                    return false;
                }
                ColourData data;
                try {
                    data = ColourData.valueOf(args[0]);
                } catch (Exception e) {
                    player.sendMessage(LIST_TEAMS.apply(commands.get(0)));
                    return false;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                gameMap.getTeamSlots().put(data, slot);
                return true;
            }
            return false;
        });
        add("solospawn", "", (player, map, args) -> {
            if(args.length == 0) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                if(gameMap.isTeam()) {
                    player.sendMessage(Chat.RED + "This map isn't a solo map.");
                    return false;
                }
                gameMap.getSoloSpawns().add(new GameLocation(player.getLocation()));
                return true;
            }
            return false;
        });
        add("generatechests", "", (player, map, args) -> {
            if(args.length == 0) {
                SkywarsGameMap gameMap = (SkywarsGameMap) map;
                if(gameMap.getIslands().isEmpty()) {
                    player.sendMessage(Chat.RED + "You must complete the islands first.");
                    return false;
                }
                gameMap.getTieredChests().clear();
                GameLocation p1 = gameMap.getBorder().getPos1(), p2 = gameMap.getBorder().getPos2();
                if(p1 != null && p2 != null && gameMap.getWorld() != null) {
                    GameArea border = new GameArea(p1.getX(), p2.getX(), p1.getY(), p2.getY(), p1.getZ(), p2.getZ());
                    Map<Material, List<GameLocation>> locationMap = getMaterialsInArea(gameMap, border, Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST);
                    gameMap.getTieredChests().put(1, locationMap.get(Material.CHEST));
                    gameMap.getTieredChests().put(2, locationMap.get(Material.TRAPPED_CHEST));
                    gameMap.getTieredChests().put(3, locationMap.get(Material.ENDER_CHEST));
                    for (GameArea area : gameMap.getIslands().keySet()) {
                        gameMap.getIslands().get(area).clear();
                        for (GameLocation location : gameMap.getTieredChests().get(1)) {
                            if(area.inside(location.getX(), location.getY(), location.getZ())) {
                                gameMap.getIslands().get(area).add(location);
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }, Chat.GREEN + "Successfully generated chests");
        add("island", "", (player, map, args) -> {
            if(args.length == 0) {
                SkywarsGameMap gameMap = (SkywarsGameMap) gameMaps.get(map);
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    GameArea area = new GameArea(pos1, pos2);
                    gameMap.getIslands().put(area, Lists.newArrayList());
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
    }

    private Map<Material, List<GameLocation>> getMaterialsInArea(GameMap gameMap, GameArea area, Material... materials) {
        Map<Material, List<GameLocation>> locationMap = new HashMap<>();
        for (Material material : materials) {
            locationMap.put(material, new ArrayList<>());
        }
        for(int y = (int) area.getYMin(); y <= area.getYMax(); y++) {
            for(int z = (int) area.getZMin(); z <= area.getZMax(); z++) {
                for(int x = (int) area.getXMin(); x <= area.getXMax(); x++) {
                    Block block = Bukkit.getWorld(gameMap.getWorld()).getBlockAt(x, y, z);
                    for (Material material : materials) {
                        if(block.getType() == material) {
                            locationMap.get(material).add(new GameLocation(block.getX(), block.getY(), block.getZ(), 0.0f ,0.0f));
                        }
                    }
                }
            }
        }
        return locationMap;
    }

    @Override
    public GameMap create(String name) {
        File file = new File(CartographerPlugin.getPlugin().getDataFolder(), "Skywars");
        GameMap map = GameMap.create("sw", new File(file, name + ".yml"));
        gameMaps.put(name, map);
        return map;
    }
}
