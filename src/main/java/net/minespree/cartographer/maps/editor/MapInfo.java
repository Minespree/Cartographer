package net.minespree.cartographer.maps.editor;

import lombok.Getter;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.ItemBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class MapInfo {

    private String name;
    private ItemBuilder builder;
    private Object object;
    private Consumer<Object> set;
    private BiConsumer<Player, Object> click;
    private String[] details;

    public MapInfo(String name, Object object, String... details) {
        this(name, object, null, null, details);
    }

    public MapInfo(String name, Object object, Consumer<Object> set, String... details) {
        this(name, object, set, null, details);
    }

    public MapInfo(String name, Object object, Consumer<Object> set, BiConsumer<Player, Object> click, String... details) {
        this.name = name;
        this.builder = new ItemBuilder(Material.STAINED_CLAY).displayName(Chat.GOLD + name);
        this.object = object;
        this.set = set;
        this.click = click;
        this.details = details;
    }

    private int getCompletion() {
        if(object == null) {
            return 0;
        } else if(object instanceof String || object instanceof Number || object instanceof Boolean) {
            builder.lore(" ");
            builder.lore(Chat.GRAY + object);
            return 2;
        } else if(object instanceof Collection) {
            builder.lore(" ");
            builder.lore(Chat.GRAY + ((Collection) object).size());
            if(((Collection) object).size() > 0) {
                return 1;
            } else return 0;
        } else if(object instanceof Map) {
            builder.lore(" ");
            builder.lore(Chat.GRAY + ((Map) object).size());
            if(((Map) object).size() > 0) {
                return 1;
            } else return 0;
        } else if(object instanceof Pair) {
            if(((Pair) object).getKey() != null && ((Pair) object).getValue() != null) {
                builder.lore(Chat.GRAY + ((Pair) object).getKey().toString() + ", " + ((Pair) object).getValue().toString());
                return 2;
            }
        }
        return 2;
    }

    public ItemStack build(Player player) {
        builder.clearLore();
        switch (getCompletion()) {
            case 0:
                builder.durability((short) 14);
                break;
            case 1:
                builder.durability((short) 4);
                break;
            case 2:
                builder.durability((short) 5);
                break;
        }
        if(details.length > 0) {
            builder.lore(" ");
            for (String detail : details) {
                builder.lore(Chat.RED + detail);
            }
        }
        return builder.build(player);
    }

}
