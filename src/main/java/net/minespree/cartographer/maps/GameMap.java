package net.minespree.cartographer.maps;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.maps.editor.MapInfo;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class GameMap {

    @Getter
    private String fileName;

    // Global consistent data for every game.
    @Setter @Getter
    protected String name, author, world;
    @Setter @Getter
    protected GameLocation spectatorLocation;
    @Setter @Getter
    protected Integer minPlayers, maxPlayers;
    @Setter @Getter
    protected long time = 6000;

    @Getter
    private File configFile;
    protected YamlConfiguration configuration;

    @Getter
    protected List<MapInfo> info = Lists.newArrayList();
    @Getter
    private ItemBuilder builder;
    @Getter
    private boolean local;

    public GameMap(File map, ItemBuilder builder) {
        this.fileName = map.getName().replaceAll(".yml", "");
        this.builder = builder.displayName(Chat.GOLD + fileName);

        this.configFile = map;
        try {
            map.getParentFile().mkdirs();
            configFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadConfig();

        local = true;
    }

    public void save() {
        if(name != null)
            configuration.set("name", name);
        if(author != null)
            configuration.set("author", author);
        if(world != null)
            configuration.set("world", world);
        if(spectatorLocation != null)
            spectatorLocation.save(configuration.createSection("spectatorLocation"));
        if(minPlayers != null)
            configuration.set("players.min", minPlayers);
        if(maxPlayers != null)
            configuration.set("players.max", maxPlayers);
        configuration.set("time", time);
        local = false;
    }

    public void load() {
        if(configuration.contains("name"))
            this.name = configuration.getString("name");
        if(configuration.contains("author"))
            this.author = configuration.getString("author");
        if(configuration.contains("world"))
            this.world = configuration.getString("world");
        if(configuration.contains("spectatorLocation"))
            this.spectatorLocation = new GameLocation(configuration.getConfigurationSection("spectatorLocation"));
        if(configuration.contains("players.min"))
            this.minPlayers = configuration.getInt("players.min");
        if(configuration.contains("players.max"))
            this.maxPlayers = configuration.getInt("players.max");
        if(configuration.contains("time"))
            time = configuration.getLong("time");
        local = false;
    }

    public void info() {
        info.clear();

        infoItem("Name", name, o -> name = (String) o);
        infoItem("Author", author, o -> author = (String) o);
        infoItem("World", world, o -> world = (String) o);
        infoItem("Spectator Location", spectatorLocation);
        infoItem("Minimum Players", minPlayers, o -> minPlayers = (Integer) o);
        infoItem("Maximum Players", maxPlayers, o -> maxPlayers = (Integer) o);
    }

    protected void infoItem(String name, Object object, BiConsumer<Player, Object> set, String... details) {
        info.add(new MapInfo(name, object, null, set, details));
    }

    protected void infoItem(String name, Object object, Consumer<Object> set, String... details) {
        info.add(new MapInfo(name, object, set, details));
    }

    protected void infoItem(String name, Object object, String... details) {
        info.add(new MapInfo(name, object, details));
    }

    public void saveConfig() {
        try {
            configuration.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        configuration = YamlConfiguration.loadConfiguration(configFile);
    }

    public static GameMap create(String gameName, File map) {
        switch (gameName) {
            case "bw":
                return new BlockwarsGameMap(map);
            case "sw":
                return new SkywarsGameMap(map);
            case "th":
                return new ThimbleGameMap(map);
            case "cl":
                return new ClashGameMap(map);
            case "dt":
                return new DoubletroubleGameMap(map);
            default:
                return null;
        }
    }

}
