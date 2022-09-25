package autoswitch.config.io;

public interface ConfigWritable {
    String configEntry();

    String separator();

    default boolean chainable() {
        return false;
    }
}
