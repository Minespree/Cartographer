package net.minespree.cartographer.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import net.minespree.cartographer.CartographerPlugin;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.cartographer.util.MapEditorWand;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.TriFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MapCommand implements CommandExecutor {


    protected static final Function<String, String> NO_MAP = s -> Chat.RED + "There is no map named " + s + " currently being edited.";
    protected static final Function<String, String> LIST_TEAMS = s -> Chat.RED + "Please see a list of teams with /" + s + " listteams";
    protected static final Function<String, String> USE_WAND = s -> Chat.RED + "Finish selecting the area with the /" + s + " wand.";
    protected static final String FAILED_COMMAND = Chat.RED + "This command does not exist.";
    protected static final String USE_NUMBER = Chat.RED + "Must input numbers.";

    protected Map<UUID, String> selectedMap = Maps.newHashMap();
    private final Map<String, MapData> commandMap = Maps.newHashMap();
    @Getter
    protected static final Map<String, GameMap> gameMaps = Maps.newHashMap();

    protected final List<String> commands = Lists.newArrayList();

    public MapCommand(String... commands) {
        this.commands.addAll(Arrays.asList(commands));

        add("wand", "", (player, args) -> {
            MapEditorWand.give(player);
            return true;
        }, "");
        add("arguments", "", (player, args) -> {
            sendArguments(player);
            return true;
        }, "");
        add("listteams", "", (player, args) -> {
            StringBuilder builder = new StringBuilder();
            for (ColourData data : ColourData.values()) {
                builder.append(data.getChatColor()).append(data.name().toLowerCase()).append(", ");
            }
            player.sendMessage(builder.subSequence(0, builder.length() - 2).toString());
            return true;
        }, "");
        add("select", "<file-name>", (player, args) -> {
            if(args.length == 1) {
                String map = args[0];
                if(gameMaps.containsKey(map)) {
                    selectedMap.put(player.getUniqueId(), map);
                    player.sendMessage(Chat.GREEN + "Selected map " + map + ".");
                    return true;
                } else {
                    NO_MAP.apply(map);
                }
            }
            return false;
        }, "");
        add("create", "<file-name>", (player, args) -> {
            if (args.length == 1) {
                create(args[0].toLowerCase());
                return true;
            }
            return false;
        }, Chat.GREEN + "Started creation.");
        add("load", "<file-name>", (player, args) -> {
            if(args.length == 1) {
                load(args[0].toLowerCase());
                return true;
            }
            return false;
        }, Chat.GREEN + "Successfully loaded");
        add("save", "<file-name>", (player, args) -> {
            if(args.length == 1) {
                String map = args[0].toLowerCase();
                if(gameMaps.containsKey(map)) {
                    gameMaps.get(map).save();
                    gameMaps.get(map).saveConfig();
                    return true;
                } else player.sendMessage(NO_MAP.apply(map));
            }
            return false;
        }, Chat.GREEN + "Successfully saved.");
        add("info", "", (player, args) -> {
            if(args.length == 0) {
                CartographerPlugin.getPlugin().getMapEditor().open(player);
                return true;
            }
            return false;
        }, "");
        add("name", "<name>", (player, map, args) -> {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg).append(" ");
            }
            map.setName(builder.toString().trim());
            return true;
        });
        add("author", "<author>", (player, map, args) -> {
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg).append(" ");
            }
            map.setAuthor(builder.toString().trim());
            return true;
        });
        add("world", "<world>", (player, map, args) -> {
            if(args.length == 1) {
                map.setWorld(args[0]);
            }
            return false;
        });
        add("spectator", "", (player, map, args) -> {
            if(args.length == 0) {
                map.setSpectatorLocation(new GameLocation(player.getLocation()));
                return true;
            }
            return false;
        });
        add("players", "<minimum> <maximum>", (player, map, args) -> {
            if(args.length == 2) {
                int min, max;
                try {
                    min = Integer.parseInt(args[2]);
                    max = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(USE_NUMBER);
                    return false;
                }
                map.setMinPlayers(min);
                map.setMaxPlayers(max);
                return true;
            }
            return false;
        });
        add("time", "<time>", (player, map, args) -> {
            if(args.length == 1) {
                try {
                    map.setTime(Long.parseLong(args[2]));
                } catch (NumberFormatException e) {
                    player.sendMessage(Chat.RED + "Must input numbers.");
                    return false;
                }
            }
            return false;
        });

        for (String command : commands) {
            CartographerPlugin.getPlugin().getCommand(command).setExecutor(this);
        }
    }

    public void add(String argument, String format, BiFunction<Player, String[], Boolean> command, String... customMessages) {
        commandMap.put(argument.toLowerCase(), new StandardMapData(argument.toLowerCase(), format, customMessages.length == 0 ? null : customMessages, command));
    }

    public void add(String argument, String format, TriFunction<Player, GameMap, String[], Boolean> command, String... customMessages) {
        commandMap.put(argument.toLowerCase(), new SpecificMapData(argument.toLowerCase(), format, customMessages.length == 0 ? null : customMessages, command));
    }

    public abstract GameMap create(String name);

    public void load(String name) {
        create(name);

        gameMaps.get(name).load();
    }

    private void sendArguments(Player player) {
        for (String s : commandMap.keySet()) {
            sendArgument(player, s, commandMap.get(s));
        }
    }

    private boolean apply(MapData data, Player player, String map, String[] args) {
        if(data instanceof StandardMapData) {
            return ((StandardMapData) data).getCommand().apply(player, args);
        } else if(data instanceof SpecificMapData) {
            return ((SpecificMapData) data).getCommand().apply(player, gameMaps.get(map), args);
        }
        return false;
    }

    private void sendArgument(Player player, String s, MapData data) {
        if(data instanceof SpecificMapData) {
            player.sendMessage(Chat.GREEN + s + ": " + Chat.GRAY + "/" + commands.get(0) + " " + s + " <file-name> " + commandMap.get(s).getFormat().trim());
        } else if(data instanceof StandardMapData) {
            player.sendMessage(Chat.GREEN + s + ": " + Chat.GRAY + "/" + commands.get(0) + " " + s + " " + commandMap.get(s).getFormat().trim());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String commandLabel, String[] args) {
        if(commands.contains(commandLabel.toLowerCase()) && sender instanceof Player && sender.isOp()) {
            Player player = (Player) sender;
            if(args.length > 0) {
                String argument = args[0];
                if (commandMap.containsKey(argument.toLowerCase())) {
                    MapData data = commandMap.get(argument.toLowerCase());
                    String[] shortArgs;
                    String map;
                    if(selectedMap.containsKey(player.getUniqueId()) || data instanceof StandardMapData) {
                        map = selectedMap.getOrDefault(player.getUniqueId(), null);
                        shortArgs = new String[args.length - 1];
                        System.arraycopy(args, 1, shortArgs, 0, args.length - 1);
                    } else {
                        if(args.length >= 2) {
                            if (gameMaps.containsKey(args[1])) {
                                map = args[1];
                                shortArgs = new String[args.length - 2];
                                System.arraycopy(args, 2, shortArgs, 0, args.length - 2);
                            } else {
                                sender.sendMessage(NO_MAP.apply(args[1]));
                                return true;
                            }
                        } else {
                            sendArgument(player, args[0].toLowerCase(), data);
                            return true;
                        }
                    }
                    if (apply(data, player, map, shortArgs)) {
                        if (data.getCustomMessages() == null) {
                            sender.sendMessage(Chat.GREEN + "Successfully setup " + args[0].toLowerCase());
                        } else {
                            for (String message : data.getCustomMessages()) {
                                player.sendMessage(message);
                            }
                        }
                    } else {
                        sendArgument(player, args[0].toLowerCase(), data);
                    }
                } else {
                    player.sendMessage(FAILED_COMMAND);
                }
            } else {
                sendArguments((Player) sender);
            }
        }
        return true;
    }

    @Getter @Data
    public abstract class MapData {
        private final String argument;
        private final String format;
        private final String[] customMessages;
    }

    @Getter
    public class StandardMapData extends MapData {
        private final BiFunction<Player, String[], Boolean> command;

        public StandardMapData(String argument, String format, String[] customMessages, BiFunction<Player, String[], Boolean> command) {
            super(argument, format, customMessages);

            this.command = command;
        }
    }

    @Getter
    public class SpecificMapData extends MapData {
        private final TriFunction<Player, GameMap, String[], Boolean> command;

        public SpecificMapData(String argument, String format, String[] customMessages, TriFunction<Player, GameMap, String[], Boolean> command) {
            super(argument, format, customMessages);

            this.command = command;
        }
    }
}