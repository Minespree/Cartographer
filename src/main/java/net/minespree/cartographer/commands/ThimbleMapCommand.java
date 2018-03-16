package net.minespree.cartographer.commands;

import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.maps.ThimbleGameMap;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.cartographer.util.MapEditorWand;

import java.io.File;

public class ThimbleMapCommand extends MapCommand {

    public ThimbleMapCommand() {
        super("thimble", "th");

        add("jumplocation", "", (player, map, args) -> {
            if(args.length == 0) {
                ((ThimbleGameMap) map).setJumpLocation(new GameLocation(player.getLocation()));
            }
            return false;
        });
        add("waitinglocation", "", (player, map, args) -> {
            if(args.length == 0) {
                ((ThimbleGameMap) map).setWaitingLocation(new GameLocation(player.getLocation()));
            }
            return false;
        });
        add("waterarea", "", (player, map, args) -> {
            if(args.length == 0) {
                ThimbleGameMap gameMap = (ThimbleGameMap) map;
                GameLocation pos1 = MapEditorWand.getPos1(player), pos2 = MapEditorWand.getPos2(player);
                if(pos1 != null && pos2 != null) {
                    gameMap.setWaterArea(new GameArea(pos1, pos2));
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
        GameMap map = GameMap.create("th", new File(CartographerPlugin.getPlugin().getDataFolder(), "Thimble/" + name + ".yml"));
        gameMaps.put(name, map);
        return map;
    }
}
