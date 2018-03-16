package net.minespree.cartographer.maps.editor;

import com.google.common.collect.Maps;
import net.minespree.babel.Babel;
import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.commands.MapCommand;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.feather.player.rank.Rank;
import net.minespree.wizard.gui.AuthenticationGUI;
import net.minespree.wizard.gui.GUI;
import net.minespree.wizard.gui.InventoryGUI;
import net.minespree.wizard.gui.MultiPageGUI;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.ItemBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class MapEditor implements Listener {

    private Map<UUID, BiFunction<Player, String, Boolean>> editing = Maps.newHashMap();

    public MapEditor() {
        Bukkit.getPluginManager().registerEvents(this, CartographerPlugin.getPlugin());
    }

    public void open(Player player) {
        MultiPageGUI mainGui = new MultiPageGUI(Babel.translate("Maps"), MultiPageGUI.PageFormat.RECTANGLE3, 45, 44, 36);

        for (GameMap map : MapCommand.getGameMaps().values()) {
            load(mainGui, map);
        }

        mainGui.open(player);
    }

    public void load(MultiPageGUI mainGui, GameMap map) {
        mainGui.addItem(player -> map.getBuilder().build(player), map, (player, type) -> open(mainGui, player, map));
    }

    private void open(MultiPageGUI mainGui, Player pl, GameMap map) {
        map.info();
        MultiPageGUI gui = new MultiPageGUI(Babel.translate(map.getName() + " details"), MultiPageGUI.PageFormat.RECTANGLE3, 45, 44, 36);
        gui.setItem(3, new ItemBuilder(Material.JUKEBOX).displayName(Chat.RED + "Load"), (player, type) -> {
            map.load();
            gui.refresh();
            player.sendMessage(Chat.GREEN + "Loaded " + map.getFileName() + ".");
            player.closeInventory();
        });
        gui.setItem(4, getMapInfo(map));
        gui.setItem(5, new ItemBuilder(Material.RECORD_4).displayName(Chat.GREEN + "Save"), (player, type) -> {
            map.save();
            map.saveConfig();
            gui.refresh();
            player.sendMessage(Chat.GREEN + "Saved " + map.getFileName() + ".");
            player.closeInventory();
        });
        gui.setItem(0, new ItemBuilder(Material.TNT).displayName(Chat.RED + "Delete"), (player, type) -> {
            player.closeInventory();
            if(NetworkPlayer.of(player).getRank().has(Rank.ADMIN)) {
                AuthenticationGUI.authenticate(player, Babel.messageStatic("Delete " + map.getName()), gui, null, p -> {
                    map.save();
                    map.saveConfig();

                    try {
                        FileUtils.copyFileToDirectory(map.getConfigFile(), new File(CartographerPlugin.getPlugin().getDataFolder(), "Deleted"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        p.sendMessage(Chat.RED + "Failed to delete " + map.getFileName() + ".");
                        return;
                    }
                    map.getConfigFile().delete();
                    MapCommand.getGameMaps().remove(map.getFileName());

                    p.sendMessage(Chat.RED + "Deleted " + map.getFileName() + ".");
                }, gui::open);
            } else {
                player.sendMessage(Chat.RED + "Request an admin if you need to delete a map.");
            }
        });
        gui.setItem(40, new ItemBuilder(Material.BOOK).displayName(Chat.GRAY + "Go Back"), (player, type) -> mainGui.open(player));
        for (MapInfo info : map.getInfo()) {
            gui.addItem(info::build, info, (p, type) -> click(map, info, gui, pl));
        }
        gui.open(pl);
    }

    private void click(GameMap map, MapInfo info, GUI parent, Player p) {
        if(info.getObject() instanceof Collection) {
            openMenu(p, parent, map, info, (Collection) info.getObject());
        } else if(info.getObject() instanceof Map) {
            MultiPageGUI data = new MultiPageGUI(Babel.translate(map.getName() + " - " + info.getName().toLowerCase()), MultiPageGUI.PageFormat.RECTANGLE3, 45, 44, 36);
            data.setItem(40, new ItemBuilder(Material.BOOK).displayName(Chat.GRAY + "Go Back"), (pl, type) -> parent.open(pl));
            Map m = (Map) info.getObject();
            for (Object o : m.keySet()) {
                data.addItem(player -> new ItemBuilder(Material.STAINED_CLAY).durability((short) 14).displayName(toString(o)).build(player), o, (player, t) -> {
                    if(t.isLeftClick()) {
                        if(info.getClick() != null) {
                            info.getClick().accept(player, o);
                        } else {
                            click(map, new MapInfo(info.getName(), m.get(o)), data, player);
                        }
                    } else if(t.isRightClick()) {
                        m.remove(o);
                        parent.open(player);
                    }
                });
            }
            data.open(p);
        } else if(info.getObject() instanceof Boolean) {
            InventoryGUI data = new InventoryGUI(map.getName() + " - " + info.getName().toLowerCase(), 45, CartographerPlugin.getPlugin());
            data.setItem(4, getMapInfo(map), player -> {});
            data.setItem(21, new ItemBuilder(Material.STAINED_CLAY).displayName(Chat.GREEN + "True").durability((short) 5), player -> info.getSet().accept(true));
            data.setItem(23, new ItemBuilder(Material.STAINED_CLAY).displayName(Chat.GREEN + "False").durability((short) 14), player -> info.getSet().accept(false));
            data.setItem(40, new ItemBuilder(Material.BOOK).displayName(Chat.GRAY + "Go Back"), parent::open);
            data.open(p);
        } else if(info.getObject() instanceof String || info.getObject() instanceof Number) {
            p.closeInventory();
            p.sendMessage(Chat.RED + (info.getObject() instanceof String ? "Input the edited message." : "Input the edited number."));
            editing.put(p.getUniqueId(), (player, s) -> {
                if(info.getObject() instanceof String) {
                    info.getSet().accept(s);
                    map.info();
                    return true;
                } else {
                    try {
                        Number number;
                        if(info.getObject() instanceof Integer) {
                            number = Integer.parseInt(s);
                        } else if(info.getObject() instanceof Long) {
                            number = Long.parseLong(s);
                        } else if(info.getObject() instanceof Double) {
                            number = Double.parseDouble(s);
                        } else if(info.getObject() instanceof Float) {
                            number = Float.parseFloat(s);
                        } else return false;
                        info.getSet().accept(number);
                        map.info();
                        return true;
                    } catch (NumberFormatException e) {
                        p.sendMessage(Chat.RED + "Input numbers only.");
                        return false;
                    }
                }
            });
        } else if(info.getObject() instanceof GameLocation || info.getObject() instanceof Location || info.getObject() instanceof GameArea) {
            location(map, p, info.getObject());
        } else {
            info.getClick().accept(p, info.getObject());
        }
    }

    private String toString(Object object) {
        if(object instanceof String || object instanceof Number) {
            return "" + object;
        } else if(object instanceof Boolean) {
            return ((Boolean) object) ? "True" : "False";
        } else if(object instanceof ColourData) {
            ColourData data = ((ColourData) object);
            return data.getChatColor() + data.getMessage().toString();
        } else if(object instanceof Pair) {
            return toString(((Pair) object).getKey()) + ", " + toString(((Pair) object).getValue());
        }
        return object.toString();
    }

    private void openMenu(Player p, GUI parent, GameMap map, MapInfo info, Collection collection) {
        MultiPageGUI data = new MultiPageGUI(Babel.translate(map.getName() + " - " + info.getName().toLowerCase()), MultiPageGUI.PageFormat.RECTANGLE3, 45, 44, 36);
        int i = 1;
        for(Object object : collection) {
            final int j = i;
            data.addItem(player -> new ItemBuilder(Material.STAINED_CLAY).durability((short) 14).displayName(Chat.GOLD + j).build(player), object, (player, t) -> {
                if(t.isLeftClick()) {
                    if(info.getClick() != null) {
                         info.getClick().accept(p, object);
                    } else {
                        location(map, player, object);
                    }
                } else if(t.isRightClick()) {
                    collection.remove(object);
                    parent.open(player);
                }
            });
            i++;
        }
        data.setItem(40, new ItemBuilder(Material.BOOK).displayName(Chat.GRAY + "Go Back"), (pl, type) -> parent.open(pl));
        data.open(p);
    }

    private void location(GameMap map, Player player, Object object) {
        if(object instanceof GameLocation) {
            player.closeInventory();
            Location location = ((GameLocation) object).toLocation();
            location.setWorld(Bukkit.createWorld(new WorldCreator(map.getWorld())));
            player.teleport(location);
        } else if(object instanceof Location) {
            player.closeInventory();
            ((Location) object).setWorld(Bukkit.createWorld(new WorldCreator(map.getWorld())));
            player.teleport(((Location) object));
        } else if(object instanceof GameArea) {
            player.closeInventory();
            Location location = ((GameArea) object).randomLocation();
            location.setWorld(Bukkit.createWorld(new WorldCreator(map.getWorld())));
            player.teleport(location);
        }
    }

    private ItemBuilder getMapInfo(GameMap map) {
        return new ItemBuilder(Material.SIGN).displayName(Chat.GOLD + "Map Info")
                .lore(Chat.DARK_GRAY + "Name: " + Chat.GREEN + map.getName())
                .lore(Chat.DARK_GRAY + "Author: " + Chat.GREEN + map.getAuthor())
                .lore(Chat.DARK_GRAY + "World: " + Chat.GREEN + map.getWorld())
                .lore(Chat.DARK_GRAY + "Min Players: " + Chat.GREEN + map.getMinPlayers())
                .lore(Chat.DARK_GRAY + "Max Players: " + Chat.GREEN + map.getMaxPlayers());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if(editing.containsKey(event.getPlayer().getUniqueId())) {
            if(editing.get(event.getPlayer().getUniqueId()).apply(event.getPlayer(), event.getMessage())) {
                editing.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Chat.GREEN + "Rokay Raggy your item has been edided, preese give me the scooby snaks.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        editing.remove(event.getPlayer().getUniqueId());
    }

}
