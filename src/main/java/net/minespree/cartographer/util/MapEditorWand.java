package net.minespree.cartographer.util;

import net.minespree.cartographer.CartographerPlugin;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapEditorWand implements Listener, CommandExecutor {

    private final static ItemStack WAND_ITEM = new ItemBuilder(Material.STICK).displayName(Chat.GOLD + "Map Wand").build();

    private final static String POS_1 = Chat.GREEN + "Set position 1.";
    private final static String POS_2 = Chat.GREEN + "Set position 2.";

    private static Map<UUID, GameLocation> pos1 = new HashMap<>(), pos2 = new HashMap<>();

    public MapEditorWand() {
        Bukkit.getPluginManager().registerEvents(this, CartographerPlugin.getPlugin());
        CartographerPlugin.getPlugin().getCommand("mapwand").setExecutor(this);
    }

    public static void give(Player player) {
        player.getInventory().addItem(WAND_ITEM);
    }

    public static GameLocation getPos1(Player player) {
        return pos1.getOrDefault(player.getUniqueId(), null);
    }

    public static GameLocation getPos2(Player player) {
        return pos2.getOrDefault(player.getUniqueId(), null);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().isSimilar(WAND_ITEM)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                pos2.put(event.getPlayer().getUniqueId(), new GameLocation(event.getClickedBlock().getLocation()));
                event.getPlayer().sendMessage(POS_2);
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                pos1.put(event.getPlayer().getUniqueId(), new GameLocation(event.getClickedBlock().getLocation()));
                event.getPlayer().sendMessage(POS_1);
            }
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(commandLabel.equalsIgnoreCase("mapwand") && sender.isOp() && sender instanceof Player) {
            if(args.length == 1) {
                Player player = (Player) sender;
                if(args[0].equalsIgnoreCase("pos1")) {
                    pos1.put(player.getUniqueId(), new GameLocation(player.getLocation()));
                    player.sendMessage(POS_1);
                    return true;
                } else if(args[0].equalsIgnoreCase("pos2")) {
                    pos2.put(player.getUniqueId(), new GameLocation(player.getLocation()));
                    player.sendMessage(POS_2);
                    return true;
                }
            }
            sender.sendMessage(Chat.RED + "That command does not exist.");
        }
        return true;
    }
}
