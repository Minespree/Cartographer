package net.minespree.cartographer;

import lombok.Getter;
import net.minespree.cartographer.commands.*;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.maps.editor.MapEditor;
import net.minespree.cartographer.util.MapEditorWand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CartographerPlugin extends JavaPlugin {

    @Getter
    private static CartographerPlugin plugin;

    @Getter
    private MapEditor mapEditor;

    public void onEnable() {
        plugin = this;

        mapEditor = new MapEditor();

        new MapEditorWand();

        new SkywarsMapCommand();
        new BlockwarsMapCommand();
        new ThimbleMapCommand();
        new DoubletroubleMapCommand();
        new ClashMapCommand();

        File directory = CartographerPlugin.getPlugin().getDataFolder();
        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && !file.getName().equals("Deleted")) {
                        File[] files2 = file.listFiles(f -> f.getName().endsWith(".yml"));
                        if (files2 != null) {
                            for (File f : files2) {
                                GameMap map = GameMap.create(f.getName().split("-")[0], f);
                                if(map != null) {
                                    MapCommand.getGameMaps().put(f.getName().replaceAll(".yml", ""), map);
                                    map.load();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
