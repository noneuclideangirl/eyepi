package net.noneuclideangirl;

import net.noneuclideangirl.data.ProcessData;
import net.noneuclideangirl.data.ServiceDescriptor;
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

    public static long getActiveServiceCount() {
        return services.size();
    }
    public static boolean isActive(ServiceDescriptor service) {
        return services.containsKey(service);
    }
    public static boolean stopService(ServiceDescriptor service) {
        if (services.containsKey(service)) {
            var processData = services.get(service).processData;
            log.info("Killing process " + processData.pid);
            return Linux.runSynchronously("kill -9 " + processData.pid)
                        .isSome();
        } else {
            return false;
        }
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
                  // Find the nth last newline
                  int lastIndex = StringUtils.countMatches("\n", out) - LOG_HISTORY_SIZE;
                  return out.substring(Math.max(0, StringUtils.ordinalIndexOf(out, "\n", lastIndex)));
              });
    }
    public static Option<Long> getServiceStartTime(ServiceDescriptor service) {
        return Option.of(services.get(service))
                .map(mon -> mon.processData.spawnTime);
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

    private String getLogStamp() {
        return "[" + new Timestamp(new Date().getTime()).toString().split("\\.")[0] + "] " + descriptor.name + " (eyepi): ";
    }

    private void run() {
        log.info(initialDocument.toJson());
        descriptor = ServiceDescriptor.fromDoc(initialDocument).unwrap();
        log.info("Monitor thread for service \"" + descriptor.name + "\" created.");

        if (!isActive()) {
            try (var fw = new FileWriter(getLogPath())) {
                Process process = Runtime.getRuntime().exec(descriptor.exec);
                var pid = process.pid();
                var spawnTime = Instant.now().getEpochSecond() * 1000;

                services.put(descriptor, this);
                processData = new ProcessData(pid, spawnTime);
                var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int c;

                fw.write(getLogStamp());
                while ((c = br.read()) != -1) {
                    synchronized (outputBuffer) {
                        outputBuffer.append(Character.toChars(c));
                    }
                    fw.write(c);

                    if (c == '\n') {
                        fw.flush();
                        fw.write(getLogStamp());
                    }
                }
                process.waitFor();
                log.warn("Service \"" + descriptor.name + "\" exited.");
                process.destroy();
                services.remove(descriptor);
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

    private boolean isActive() {
        if (!services.containsKey(descriptor)) {
            return false;
        } else {
            var query = Linux.runSynchronously("ps | grep " + services.get(descriptor) + " | awk '{ print $1 }'");
            return query.map(result -> result.length() > 0).unwrapOr(false);
        }
    }
}
