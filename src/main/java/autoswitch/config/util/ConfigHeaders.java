package autoswitch.config.util;

public final class ConfigHeaders {

    public static final String basicConfig = """
            AutoSwitch Configuration File
            See https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details.
            Setting an option to 'true' will enable it's stated behavior; using 'false' will disable it.""";
    private static final String format = """

            Format is a comma separated list of 'tool selectors', which take the form of 'toolname[;enchantment list]',\s
            where toolname is either a ToolGrouping, or a specific item id with same formatting rules as enchantments. \s
            Enchant list is optional. If present, it must be separated from the tool by a semicolon (';'). Enchantment list is a list of `enchantment id`s separated by `&`, such as 'minecraft!fortune&minecraft!mending'. The list can have infinite enchantments. If multiple enchantments are specified, only tools with *all* of the enchantments will be selected. Use multiple tool selectors to specify optional enchantments. See toolEnchantmentsStack in autoswitch.cfg.\s
            Enchant and item id uses '!' instead of colons. A colon can be used as long as it is on the right of the equal sign.
            Example: minecraft!stick;minecraft!fortune\s
            List is ordered and will affect tool selection.\s
            You can add block/mob-specific overrides by adding it's id (replacing colon with '\\:' or '!') on a new line and adding values to the right of\s
            the equals sign ('=') as you would normally. Check the boat for an example.\s
            Removing the values after the equals sign will disable the switch. In the case of overrides, the line may be removed for default behavior.
            If you would like to write config values across multiple lines, end the preceding line with '\\', followed by a new line. See https://stackoverflow.com/a/8978515
            For further information such as tag selectors, see https://github.com/dexman545/Fabric-Autoswitch/wiki/Config-Details""";
    public static final String attackConfig = "AutoSwitch Material Configuration File" + format;

    public static final String usableConfig = "AutoSwitch Usable Configuration File" +
                                              "\nThis file defines tool switching targets for the 'use/interact' " +
                                              "action (right clicking) on a mob or block. " +
                                              "\nSame format as the Material Config:" + format;

    public static final String eventConfig = format; //todo adapt for events


    private ConfigHeaders() {
    }

}
