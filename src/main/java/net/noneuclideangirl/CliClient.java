package net.noneuclideangirl;

import net.noneuclideangirl.util.ConfigManager;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CliClient {
    public static void main(String[] args) {
        ConfigManager.loadProperties("/var/eyepi/eyepi.properties");
        var maybePort = ConfigManager.getInt("port");
        if (!maybePort.isSome()) {
            System.out.println("Config file missing required field: port");
            System.exit(-1);
        }

        Socket socket = null;
        try {
            socket = new Socket("localhost", maybePort.unwrap());
        } catch (IOException e) {
            String message = "";
            if (e.getMessage() != null) {
                message = String.format("(%s)", e.getMessage());
            }
            System.out.format("Failed to connect to server on port %d: %s %s",
                              maybePort.unwrap(),
                              e.getClass().getSimpleName(),
                              message);
            System.exit(-2);
        }

        System.out.println("Connected to server.");

        Scanner scanner = new Scanner(System.in);

        System.out.print("> ");
        while (scanner.hasNextLine()) {
            var input = scanner.nextLine();
            switch (input.strip()) {
                case "new":
                    newService(scanner, socket);
                    break;

                default:
                    System.out.format("Unrecognised command: %s" + input);
                    break;
            }

            System.out.print("> ");
        }
    }

    private static void newService(Scanner scanner, Socket socket) {
        final String defaultName = "Untitled Service";
        final String defaultDesc = "<no description>";
        final String defaultExec = "cat /dev/null";

        System.out.format("Name (%s): ", defaultName);
        if (!scanner.hasNextLine()) {
            System.exit(0);
        }
        var name = scanner.nextLine();
        if (name.isBlank()) {
            name = defaultName;
        }

        System.out.format("Description (%s): ", defaultDesc);
        if (!scanner.hasNextLine()) {
            System.exit(0);
        }
        var desc = scanner.nextLine();
        if (desc.isBlank()) {
            desc = defaultDesc;
        }

        System.out.format("Command (%s): ", defaultExec);
        if (!scanner.hasNextLine()) {
            System.exit(0);
        }
        var exec = scanner.nextLine();
        if (exec.isBlank()) {
            exec = defaultExec;
        }

        name = name.replaceAll("\"", "\\\"");
        desc = desc.replaceAll("\"", "\\\"");
        exec = exec.replaceAll("\"", "\\\"");
        try {
            socket.getOutputStream()
                  .write(String.format("{\"command\":\"CREATE_SERVICE\",\"name\":\"%s\",\"desc\":\"%s\",\"exec\":\"%s\"}\n",
                          name, desc, exec).getBytes());
        } catch (IOException e) {
            String message = "";
            if (e.getMessage() != null) {
                message = String.format("(%s)", e.getMessage());
            }
            System.out.format("Failed to write to socket: %s %s",
                    e.getClass().getSimpleName(),
                    message);
        }
    }
}
