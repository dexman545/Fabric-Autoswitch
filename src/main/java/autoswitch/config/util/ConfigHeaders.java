package autoswitch.config.util;

public interface ConfigHeaders {

    String basicConfig = "AutoSwitch Configuration File" +
            "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details." +
            "\nSetting an option to 'true' will enable it's stated behavior; using 'false' wil disable it.";

    String materialConfig = "AutoSwitch Material Configuration File" +
            "\nFormat is a comma separated list of 'tool selectors', which take the form of 'toolname[;enchantment list]', " +
            " where toolname is either a ToolGrouping, or a specific item id with same formatting rules as enchantments. " +
            "\nEnchant list is optional. If present, it must be separated from the tool by a semicolon (';'). " +
            "Enchantment list is a list of `enchantment id`s separated by `&`, such as 'minecraft-fortune&minecraft-mending'. " +
            "The list can have anywhere from 1 to infinite enchantments. If multiple enchantments are specified, only " +
            "tools with *all* of the enchantments will be selected. Use multiple tool selectors to specify optional " +
            "enchantments. See toolEnchantmentsStack in autoswitch.cfg. " +
            "\nEnchant and item id uses '-' instead of colons. A colon can be used, but must be preceded by a backslash. " +
            "\nExample: minecraft-stick;minecraft-fortune" +
            "\nList is ordered and will affect tool selection. " +
            "\nYou can add block/mob-specific overrides by adding it's id (replacing colon with '\\:' or '-') on a new line and adding values to the right of " +
            "\nthe equals sign ('=') as you would normally. Check the boat for an example. " +
            "\nRemoving the values after the equals sign will disable the switch. In the case of overrides, the line may be removed " +
            "for default behavior." +
            "\nIf you would like to write config values across multiple lines, use a '\\', followed by a new line. See https://stackoverflow.com/a/8978515";

    String usableConfig = "AutoSwitch Usable Configuration File" +
            "\nThis file defines tool switching targets for the 'use' action (right clicking) on a mob or block. " +
            "\nSame format as the Material Config:" +
            "\nFormat is a comma separated list of 'tool selectors', which take the form of 'toolname[;enchantment list]', " +
            " where toolname is either a ToolGrouping, or a specific item id with same formatting rules as enchantments. " +
            "\nEnchant list is optional. If present, it must be separated from the tool by a semicolon (';'). " +
            "Enchantment list is a list of `enchantment id`s separated by `&`, such as 'minecraft-fortune&minecraft-mending'. " +
            "The list can have anywhere from 1 to infinite enchantments. If multiple enchantments are specified, only " +
            "tools with *all* of the enchantments will be selected. Use multiple tool selectors to specify optional " +
            "enchantments. See toolEnchantmentsStack in autoswitch.cfg. " +
            "\nEnchant and item id uses '-' instead of colons. A colon can be used, but must be preceded by a backslash. " +
            "\nExample: minecraft-stick;minecraft-fortune " +
            "\nList is ordered and will affect tool selection. " +
            "\nYou can add block/mob-specific overrides by adding it's id (replacing colon with '\\:' or '-') on a new line and adding values to the right of " +
            "\nthe equals sign ('=') as you would normally. Check the boat for an example. " +
            "\nRemoving the values after the equals sign will disable the switch. In the case of overrides, the line may be removed " +
            "for default behavior." +
            "\nIf you would like to write config values across multiple lines, use a '\\', followed by a new line. See https://stackoverflow.com/a/8978515";

}
