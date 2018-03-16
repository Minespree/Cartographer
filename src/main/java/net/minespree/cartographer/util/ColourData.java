package net.minespree.cartographer.util;

import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import org.bukkit.ChatColor;
import org.bukkit.Color;

@Getter
public enum ColourData {

    BLACK("0", "BL", Babel.translate("black"), Color.fromRGB(0, 0, 0), 15),
    DARK_BLUE("1", "DB", Babel.translate("dark_blue"), Color.fromRGB(0, 0, 170), 11),
    DARK_GREEN("2", "DG", Babel.translate("dark_green"), Color.fromRGB(0, 170, 0), 13),
    DARK_AQUA("3", "DA", Babel.translate("dark_aqua"), Color.fromRGB(0, 170, 170), 9),
    DARK_RED("4", "DR", Babel.translate("dark_red"), Color.fromRGB(170, 0, 0), 14),
    DARK_PURPLE("5", "DP", Babel.translate("dark_purple"), Color.fromRGB(170, 0, 170), 10),
    GOLD("6", "GO", Babel.translate("gold"), Color.fromRGB(255, 170, 0), 1),
    GRAY("7", "GR", Babel.translate("gray"), Color.fromRGB(170, 170, 170), 8),
    DARK_GRAY("8", "DGR", Babel.translate("dark_gray"), Color.fromRGB(85, 85, 85), 7),
    BLUE("9", "B", Babel.translate("blue"), Color.fromRGB(85, 85, 255), 11),
    GREEN("a", "G", Babel.translate("green"), Color.fromRGB(85, 255, 85), 5),
    AQUA("b", "A", Babel.translate("aqua"), Color.fromRGB(85, 255, 255), 3),
    RED("c", "R", Babel.translate("red"), Color.fromRGB(255, 85, 85), 14),
    LIGHT_PURPLE("d", "P", Babel.translate("light_purple"), Color.fromRGB(255, 85, 255), 6),
    YELLOW("e", "Y", Babel.translate("yellow"), Color.fromRGB(255, 255, 85), 4),
    WHITE("f", "W", Babel.translate("white"), Color.fromRGB(255, 255, 255), 0);

    String colourCode, colourBlind;
    BabelMessage message;
    Color colour;
    ChatColor chatColor;
    short woolColour;

    ColourData(String colourCode, String colourBlind, BabelMessage message, Color colour, int woolColour) {
        this.colourCode = colourCode;
        this.colourBlind = colourBlind;
        this.message = message;
        this.colour = colour;
        this.chatColor = ChatColor.getByChar(colourCode);
        this.woolColour = (short) woolColour;
    }

}
