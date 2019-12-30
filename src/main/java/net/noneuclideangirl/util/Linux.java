package net.noneuclideangirl.util;

import net.noneuclideangirl.functional.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Linux {
    private static final Logger log = LogManager.getLogger(Linux.class);

    /**
     * Run a command and return its output (if successful).
     */
    public static Option<String> runSynchronously(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            var br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String s;
            StringBuilder output = new StringBuilder();
            while ((s = br.readLine()) != null) {
                output.append(s);
            }
            p.waitFor();
            p.destroy();
            return Option.some(output.toString());
        } catch (IOException | InterruptedException e) {
            log.error("Failed to execute command \"" + command + "\": " + e.getClass().getName() + ": " + e.getMessage());
            return Option.none();
        }
    }
}
