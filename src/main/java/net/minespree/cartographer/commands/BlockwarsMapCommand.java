package net.minespree.cartographer.commands;

import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.BlockwarsGameMap;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.cartographer.util.MapEditorWand;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class BlockwarsMapCommand extends MapCommand {

    public BlockwarsMapCommand() {
        super("blockwars", "bw");

        add("buildy", "<build-y>", (player, map, args) -> {
            if(args.length == 0) {
                int buildY;
                try {
                    buildY = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                ((BlockwarsGameMap) map).setBuildY(buildY);
                return true;
            }
            return false;
        });
        add("border", "", (player, map, args) -> {
            if(args.length == 0) {
                BlockwarsGameMap gameMap = (BlockwarsGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    GameArea area = new GameArea(pos1, pos2);
                    gameMap.setBorder(area);
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("wall", "", (player, map, args) -> {
            if(args.length == 0) {
                BlockwarsGameMap gameMap = (BlockwarsGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    GameArea area = new GameArea(pos1, pos2);
                    gameMap.setWall(area);
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("disabledarea", "", (player, map, args) -> {
            if(args.length == 0) {
                BlockwarsGameMap gameMap = (BlockwarsGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    GameArea area = new GameArea(pos1, pos2);
                    gameMap.getDisabledAreas().add(area);
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("neutralcore", "", (player, map, args) -> {
            if(args.length == 0) {
                GameLocation location = new GameLocation(player.getLocation().subtract(0, 1, 0).getBlock().getLocation());
                ((BlockwarsGameMap) map).getNeutralCores().add(location);
                return true;
            }
            return false;
        });
        add("spawnarea", "<team> <yaw> <pitch>", (player, map, args) -> {
            if(args.length == 3) {
                BlockwarsGameMap gameMap = (BlockwarsGameMap) map;
                ColourData data;
                try {
                    data = ColourData.valueOf(args[0]);
                } catch (Exception e) {
                    player.sendMessage(LIST_TEAMS.apply(commands.get(0)));
                    return false;
                }
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if (pos1 == null || pos2 == null) {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                    return false;
                }
                float yaw, pitch;
                try {
                    yaw = Float.parseFloat(args[1]);
                    pitch = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                gameMap.getTeamSpawnDirection().put(data, new ImmutablePair<>(yaw, pitch));
                gameMap.getTeamSpawns().put(data, new GameArea(pos1, pos2));
            }
            return false;
        });
        add("core", "<team> <slot>", (player, map, args) -> {
            if(args.length == 2) {
                ColourData data;
                try {
                    data = ColourData.valueOf(args[0]);
                } catch (Exception e) {
                    player.sendMessage(LIST_TEAMS.apply(commands.get(0)));
                    return false;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                GameLocation location = new GameLocation(player.getLocation().subtract(0, 1, 0).getBlock().getLocation());
                ((BlockwarsGameMap) map).getTeamCores().put(data, location);
                ((BlockwarsGameMap) map).getTeamSlots().put(data, slot);
                return true;
            }
            return false;
        });
        add("wallitem", "<id> <data>", (player, map, args) -> {
            if(args.length == 2) {
                int id, data;
                try {
                    id = Integer.parseInt(args[0]);
                    data = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                ((BlockwarsGameMap) map).getWallItems().add(new ItemStack(Material.getMaterial(id), 1, (short) data));
                return true;
            }
            return false;
        });
    }

    @Override
    public GameMap create(String name) {
        File file = new File(CartographerPlugin.getPlugin().getDataFolder(), "Blockwars");
        GameMap map = GameMap.create("bw", new File(file, name + ".yml"));
        gameMaps.put(name, map);
        return map;
    }
}
