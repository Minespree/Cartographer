package net.minespree.cartographer.commands;

import com.google.common.collect.Lists;
import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.ClashGameMap;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.cartographer.util.MapEditorWand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ClashMapCommand extends MapCommand {

    public ClashMapCommand() {
        super("clash", "cl");

        add("border", "", (player, map, args) -> {
            if(args.length == 0) {
                ClashGameMap gameMap = (ClashGameMap) map;
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
        add("disabledarea", "", (player, map, args) -> {
            if(args.length == 0) {
                ClashGameMap gameMap = (ClashGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    gameMap.getDisabledAreas().add(new GameArea(pos1, pos2));
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("spawn", "<side> <slot> <yaw> <pitch>", (player, map, args) -> {
            if(args.length == 4) {
                ClashGameMap gameMap = (ClashGameMap) map;
                float yaw, pitch;
                try {
                    yaw = Float.parseFloat(args[1]);
                    pitch = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    gameMap.getSpawns().put(args[0], new GameArea(pos1, pos2));
                    gameMap.getSpawnYawPitch().put(args[0], new ImmutablePair<>(yaw, pitch));
                    gameMap.getSlots().put(args[0], slot);
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("castle", "<side> [tower/front]", (player, map, args) -> {
            if(args.length == 2) {
                ClashGameMap gameMap = (ClashGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    if(args[1].equalsIgnoreCase("tower")) {
                        if(gameMap.getCastleAreas().containsKey(args[0])) {
                            gameMap.getCastleAreas().get(args[0]).setTower(new GameArea(pos1, pos2));
                        } else {
                            gameMap.getCastleAreas().put(args[0], new ClashGameMap.ClashMapTower("", new GameArea(pos1, pos2), null));
                        }
                    } else if(args[1].equalsIgnoreCase("front")) {
                        if(gameMap.getCastleAreas().containsKey(args[0])) {
                            gameMap.getCastleAreas().get(args[0]).setFront(new GameArea(pos1, pos2));
                        } else {
                            gameMap.getCastleAreas().put(args[0], new ClashGameMap.ClashMapTower("", null, new GameArea(pos1, pos2)));
                        }
                    } else {
                        return false;
                    }
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("tower", "<side> <id> [tower/front]", (player, map, args) -> {
            if(args.length == 3) {
                ClashGameMap gameMap = (ClashGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    List<ClashGameMap.ClashMapTower> list = gameMap.getTowerAreas().computeIfAbsent(args[0], side -> Lists.newArrayList());
                    Optional<ClashGameMap.ClashMapTower> optional = list.stream().filter(tower -> tower.getId().equalsIgnoreCase(args[1])).findFirst();
                    if(args[2].equalsIgnoreCase("tower")) {
                        if(optional.isPresent()) {
                            optional.get().setTower(new GameArea(pos1, pos2));
                        } else {
                            list.add(new ClashGameMap.ClashMapTower(args[1], new GameArea(pos1, pos2), null));
                        }
                    } else if(args[2].equalsIgnoreCase("front")) {
                        if(optional.isPresent()) {
                            optional.get().setFront(new GameArea(pos1, pos2));
                        } else {
                            list.add(new ClashGameMap.ClashMapTower(args[1], null, new GameArea(pos1, pos2)));
                        }
                    } else {
                        return false;
                    }
                    gameMap.getTowerAreas().put(args[0], list);
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
        add("side", "<side> [left/right]", (player, map, args) -> {
            if(args.length == 2) {
                ClashGameMap gameMap = (ClashGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    if(args[1].equalsIgnoreCase("left")) {
                        gameMap.getLeft().put(args[0], new GameArea(pos1, pos2));
                    } else if(args[1].equalsIgnoreCase("right")) {
                        gameMap.getRight().put(args[0], new GameArea(pos1, pos2));
                    } else return false;
                    return true;
                } else {
                    player.sendMessage(USE_WAND.apply(commands.get(0)));
                }
            }
            return false;
        });
    }

    @Override
    public GameMap create(String name) {
        File file = new File(CartographerPlugin.getPlugin().getDataFolder(), "Clash");
        GameMap map = GameMap.create("cl", new File(file, name + ".yml"));
        gameMaps.put(name, map);
        return map;
    }
}
