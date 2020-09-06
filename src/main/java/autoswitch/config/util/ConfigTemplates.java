package autoswitch.config.util;

import autoswitch.AutoSwitch;
import com.google.common.base.Strings;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Enumeration;

public class ConfigTemplates {
    private static final int width = 131 - 11;
    private static final String commentChar = "#";
    public static final String border = Strings.repeat(commentChar, width);
    private static final String baseCommentedText = "# <FILL> #";
    private static final String modCategoryDescription = "Config Entries Provided by: ";
    private static final String defaultValueComment = "\n# Default Value: ";
    private static final String deliminatorPreserver = "((?<=%1$s)|(?=%1$s))"; // selects text before/after the delim.

    public static String modCategory(String modName) {
        return category(modName, modCategoryDescription);

    }

    private static String category(String name, String categoryDesc, String... info) {
        String title = categoryDesc + name;

        if (title.length() < width - 4) {
            title = centeredString(title);
            title = title + Strings.repeat(" ", width - title.length() - 4);
        }

        ArrayList<String> out;

        if (info != null) {
            //title += "\n";
            out = new ArrayList<>();
            out.add(border);
            out.add(baseCommentedText.replace("<FILL>", title));
            for (String s : info) {
                if (s.length() < width - 4) {
                    s = centeredString(s);
                    s = s + Strings.repeat(" ", width - s.length() - 4);
                    out.add(baseCommentedText.replace("<FILL>", s));
                }
            }
            out.add(border);

            String[] a = new String[out.size()];

            return combineToBlock(out.toArray(a));
        }

        return combineToBlock(border, baseCommentedText.replace("<FILL>", title), border);

    }

    public static String configEntry(String key, String value, String comment, String defaultValue) {
        return configEntry(key, value, comment, defaultValue, false);
    }

    // Multiline wrap for the config value
    public static String configEntry(String key, String value, String comment, String defaultValue, boolean doWrap) {
        return configEntry(configValueEntry(key, value, doWrap), comment, defaultValue);
    }

    public static String toolGroupings() {
        StringBuilder keys = new StringBuilder();
        Enumeration<String> enumKeys = AutoSwitch.switchData.toolGroupings.keys();

        while (enumKeys.hasMoreElements()) {
            keys.append(keys.length() > 0 ? ", " : "").append(enumKeys.nextElement());
        }

        return category("Provided Tool Groupings by AutoSwitch and Mods it Interfaced With", "",
                "Tool groupings are a way to specify multiple tools at once. To match any grouping, use 'any'."
                , keys.toString());
    }

    /**
     * Word wrap given text for use in properties file as a comment.
     * Supports multiline text.
     */
    public static String wordWrapComment(String str) {
        StringBuilder out = new StringBuilder();
        String[] cls = str.replaceAll("\n", "\n# ").split(String.format(deliminatorPreserver, "\n"));
        for (String cl : cls) {
            out.append(WordUtils.wrap(cl, width - 2, "\n# ", false));
        }
        return out.toString();
    }

    private static String configCommentEntry(String defaultValue) {
        return (defaultValue != null && !defaultValue.equals("")) ? defaultValueComment + defaultValue : "";
    }

    private static String configEntry(String cfg, String comment, String defaultValue) {
        StringBuilder out = new StringBuilder();
        if (comment != null && !comment.equals("")) {
            out.append("\n");
            out.append(wordWrapComment(comment));
        }

        out.append(configCommentEntry(defaultValue));

        return combineToBlock(out.toString(), cfg);

    }

    private static String configValueEntry(String key, String value) {
        return configValueEntry(key, value, false);
    }

    private static String configValueEntry(String key, String value, boolean doWrap) {
        if (value == null) value = "";
        key = key.replaceAll("(?<!\\\\)(?:\\\\{2})*:", "\\:");
        return doWrap ? configValueEntryWordwrap(key + " = " + value) : key + " = " + value;
    }

    private static String configValueEntryWordwrap(String str) {
        return WordUtils.wrap(str, width - 2, ", \\\n", false, ",");
    }

    private static String centeredString(String input) {
        int centering = (int) Math.ceil((width - input.length() - 4) / 2f);

        return Strings.repeat(" ", centering) + input;
    }

    private static String combineToBlock(String... strings) {
        StringBuilder out = new StringBuilder();

        for (String string : strings) {
            out.append(string).append("\n");
        }

        out.delete(out.length() - 1, out.length());

        return out.toString();
    }
}
