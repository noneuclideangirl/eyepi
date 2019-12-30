package net.noneuclideangirl.util;

import net.noneuclideangirl.functional.Functional;
import net.noneuclideangirl.functional.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Cache {
    private static final Logger log = LogManager.getLogger(Cache.class);
    private final Map<String, CacheEntry> cacheMap = new HashMap<>();
    private static class CacheEntry {
        public final Document doc;
        public boolean valid = true;

        private CacheEntry(Document doc) {
            this.doc = doc;
        }
    }

    public void invalidate(String key) {
        Option.of(cacheMap.get(key))
              .ifSome(e -> e.valid = false);
    }

    public Document get(String key, Supplier<Document> ifMiss) {
        return Option.of(cacheMap.get(key))
              .andThen(e -> Option.ifThen(e.valid, e.doc))
              .matchThen(Functional.id(), () -> {
                  log.info("Cache miss for " + key);
                  var result = ifMiss.get();
                  cacheMap.put(key, new CacheEntry(result));
                  return result;
              });
    }
}
