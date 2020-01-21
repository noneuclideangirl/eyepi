package net.noneuclideangirl;

import net.noneuclideangirl.data.ProcessData;
import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.functional.Functional;
import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.util.Linux;
import net.noneuclideangirl.util.ThreadManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceMonitor {
    private static final int LOG_HISTORY_SIZE = 50;
    private static final Logger log = LogManager.getLogger(ServiceMonitor.class);
    private static final Map<ServiceDescriptor, ServiceMonitor> services = new ConcurrentHashMap<>();

    private ServiceDescriptor descriptor;
    private final StringBuilder outputBuffer = new StringBuilder();
    private final Document initialDocument;
    private ProcessData processData;
    private boolean active = true;

    public static long getActiveServiceCount() {
        return services.values().stream().filter(ServiceMonitor::isActive).count();
    }
    public static boolean isActive(ServiceDescriptor service) {
        return Option.of(services.get(service))
                     .map(ServiceMonitor::isActive)
                     .orElse(false);
    }
    public static boolean stopService(ServiceDescriptor service) {
        if (isActive(service)) {
            var processData = services.get(service).processData;
            log.info("Killing process " + processData.pid);
            return Linux.runSynchronously("kill -9 " + processData.pid)
                        .isSome();
        } else {
            return false;
        }
    }
    public static boolean purgeService(ServiceDescriptor service) {
        stopService(service);
        return services.remove(service) != null;
    }
    public static boolean startService(ServiceDescriptor service) {
        if (isActive(service)) {
            return false;
        } else {
            new ServiceMonitor(service);
            return true;
        }
    }
    public static Option<String> getRecentLogs(ServiceDescriptor service) {
        return Option.of(services.get(service))
              .map(mon -> {
                  String out = mon.getOutput();
                  log.debug(out.contains("\n"));
                  // Find the nth last newline
                  int lastIndex = StringUtils.countMatches(out, "\n") - LOG_HISTORY_SIZE;
                  var startFrom = StringUtils.ordinalIndexOf(out, "\n", lastIndex);

                  // Drop the leading newline
                  return out.substring(startFrom + 1);
              });
    }
    public static Option<Long> getServiceStartTime(ServiceDescriptor service) {
        return Option.of(services.get(service))
                .map(mon -> mon.processData.spawnTime);
    }

    static void changeServiceDescriptor(String id, ServiceDescriptor newDescriptor) {
        services.keySet()
                .stream()
                .filter(desc -> desc.id.matchThen(id::equals, Functional.supply(false)))
                .findFirst()
                .ifPresent(desc -> {
                    var monitor = services.get(desc);
                    services.remove(desc);
                    monitor.descriptor = newDescriptor;
                    services.put(newDescriptor, monitor);
                    log.info("Updated descriptor for service \"" + desc.name + "\" (new name " + newDescriptor.name + ")");
                });
    }

    ServiceMonitor(Document doc) {
        initialDocument = doc;
        ThreadManager.execute(this::run);
    }

    public ServiceMonitor(ServiceDescriptor service) {
        this(service.toDoc());
    }

    private String getLogPath() {
        return "/var/eyepi/logs/" + descriptor.name.replaceAll(" ", "-").toLowerCase() + ".log";
    }

    public String getOutput() {
        synchronized (outputBuffer) {
            return outputBuffer.toString();
        }
    }

    private String getTimestamp() {
        return "[" + new Timestamp(new Date().getTime()).toString().split("\\.")[0] + "] ";
    }

    private String getLogPrefix() {
        return getTimestamp() + descriptor.name + " (eyepi): ";
    }

    private boolean tryCleanupService(ServiceDescriptor service) {
        if (isActive(service)) {
            return false;
        } else {
            // Remove zombie monitor if it's there
            Option.of(services.get(service))
                  .ifSome(ServiceMonitor::cleanup);
            return true;
        }
    }

    private void run() {
        log.info(initialDocument.toJson());
        descriptor = ServiceDescriptor.fromDoc(initialDocument).unwrap();
        log.info("Monitor thread for service \"" + descriptor.name + "\" created.");

        if (tryCleanupService(descriptor)) {
            try (var fw = new FileWriter(getLogPath())) {
                Process process = Runtime.getRuntime().exec(descriptor.exec);
                var pid = process.pid();
                var spawnTime = Instant.now().getEpochSecond() * 1000;

                synchronized (outputBuffer) {
                    outputBuffer.append(getTimestamp())
                                .append("<started>\n");
                }
                fw.write(getTimestamp() + "<started>\n");

                services.put(descriptor, this);
                processData = new ProcessData(pid, spawnTime);
                var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int c;

                fw.write(getLogPrefix());
                boolean writePrefix = true;
                boolean lastWroteNewline = false;

                while ((c = br.read()) != -1) {
                    if (writePrefix) {
                        synchronized (outputBuffer) {
                            outputBuffer.append(getLogPrefix());
                        }
                        fw.write(getLogPrefix());
                        writePrefix = false;
                    }
                    synchronized (outputBuffer) {
                        outputBuffer.append(Character.toChars(c));
                    }
                    fw.write(c);

                    if (c == '\n') {
                        fw.flush();
                        writePrefix = true;
                        lastWroteNewline = true;
                    } else {
                        lastWroteNewline = false;
                    }
                }
                process.waitFor();
                active = false;
                log.warn("Service \"" + descriptor.name + "\" exited.");

                synchronized (outputBuffer) {
                    if (!lastWroteNewline) {
                        outputBuffer.append("\n");
                    }
                    outputBuffer.append(getTimestamp())
                                .append("<exited>\n");
                }
                fw.write((lastWroteNewline ? "" : "\n") + getTimestamp() + "<exited>\n");
                process.destroy();
            } catch (IOException e) {
                log.error("Service \"" + descriptor.name + "\": Failed running command \"" + descriptor.exec + "\": "
                        + e.getClass().getName() + ": " + e.getMessage());
            } catch (InterruptedException e) {
                log.warn("Service \"" + descriptor.name + "\": monitor thread interrupted");
            }
        } else {
            log.warn("Started monitor thread for service \"" + descriptor.name + "\" but service was already running.");
        }
    }

    private void cleanup() {
        services.remove(descriptor);
        log.info("Monitor thread for service \"" + descriptor.name + "\" destroyed.");
    }

    private boolean isActive() {
        return active;
    }
}
