package net.minespree.cartographer.commands;

import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.DoubletroubleGameMap;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameLocation;

import java.io.File;

public class DoubletroubleMapCommand extends MapCommand {

    public DoubletroubleMapCommand() {
        super("doubletrouble", "dt");

        add("spawn", "<team>", (player, map, args) -> {
            if (args.length == 1) {

                String s = args[0].toUpperCase();
                ColourData colourData;

                try {
                    colourData = ColourData.valueOf(s.toUpperCase());

                    DoubletroubleGameMap gameMap = (DoubletroubleGameMap) map;
                    gameMap.getTeamSpawns().put(colourData, new GameLocation(player.getLocation()));
                } catch(IllegalArgumentException e) {
                    e.printStackTrace();
                }

                return true;
            }
            return false;
        });

    }

    @Override
    public GameMap create(String name) {
        File file = new File(CartographerPlugin.getPlugin().getDataFolder(), "Doubletrouble");
        GameMap map = GameMap.create("dt", new File(file, name + ".yml"));
        gameMaps.put(name, map);
        return map;
    }

}
