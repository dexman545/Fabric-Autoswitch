package autoswitch.config.io;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import autoswitch.AutoSwitch;

import org.aeonbits.owner.loaders.Loader;

public class LenientPropertiesLoader implements Loader {
    @Serial
    private static final long serialVersionUID = -4089764841865794854L;

    public LenientPropertiesLoader() {
    }

    public boolean accept(URI uri) {
        try {
            return uri.toURL().toString().endsWith(".cfg");
        } catch (MalformedURLException | IllegalArgumentException e) {
            AutoSwitch.logger.error(uri.toString());
            AutoSwitch.logger.error("Lenient Processing", e);
            return false;
        }
    }

    public void load(Properties result, URI uri) throws IOException {
        URL url = uri.toURL();

        try (InputStream input = url.openStream()) {
            this.load(result, input);
        }
    }

    void load(Properties result, InputStream input) throws IOException {
        result.load(new FilterReader(new InputStreamReader(input, StandardCharsets.UTF_8)) {
            @Override
            public int read() throws IOException {
                int c = super.read();
                if (c == ':') {
                    c = '!';
                }
                return c;
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                int read = super.read(cbuf, off, len);
                if (read == -1) {
                    return -1;
                }
                for (int i = off; i < off + read; i++) {
                    if (cbuf[i] == ':') {
                        cbuf[i] = '!';
                    }
                }
                return read;
            }
        });
    }

    public String defaultSpecFor(String uriPrefix) {
        return uriPrefix + ".cfg";
    }
}