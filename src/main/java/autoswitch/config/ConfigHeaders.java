package autoswitch.config;

public interface ConfigHeaders {

    String basicConfig = "AutoSwitch Configuration File" +
            "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details" +
            "\nTool priority order values must match exactly with what is in the material config, both tool and enchantment";

    String materialConfig = "AutoSwitch Material Configuration File" +
            "\nformat is a comma separated list of 'toolname[;enchantment id]', where toolname is any:" +
            "\n\t any, pickaxe, shears, axe, shovel, hoe, trident, sword, or a specific item id, with same formatting rules as enchantments" +
            "\nEnchant id is optional. If present, it must be separated from the tool by a semicolon (';')" +
            "\nEnchant id uses '-' instead of colons. A colon can be used, but must be preceded by a backslash" +
            "\nList is ordered and will effect tool selection" +
            "\nYou can cadd block/mob-specific overrides by adding it's id on a new line and adding values to the right of" +
            "\nthe equals sign ('=') as you would normally. Check the boat for an example." +
            "\nRemoving the values after the equals sign will disable the switch. In the case of overrides, the line may be removed.";

    String usableConfig = "AutoSwitch Usable Configuration File" +
            "\nThis file defines tool switching targets for the 'use' action (right clicking) on a mob or block." +
            "\nSame format as the Material Config:" +
            "\nformat is a comma separated list of 'toolname[;enchantment id]', where toolname is any:" +
            "\n\t specific item id, with same formatting rules as enchantments" +
            "\nEnchant id is optional. If present, it must be separated from the tool by a semicolon (';')" +
            "\nEnchant id uses '-' instead of colons. A colon can be used, but must be preceded by a backslash" +
            "\nList is ordered and will effect tool selection" +
            "\nTo add mobs or blocks you wish to target, simply enter their id on a new line, followed by an '=' and the id of the tool you want to be used." +
            "\nRRemoving the values after the equals sign will disable the switch. In case of user added targets, removing the line will also work.";

}
