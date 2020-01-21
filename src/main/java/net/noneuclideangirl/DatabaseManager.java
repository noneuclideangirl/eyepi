package net.noneuclideangirl;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.functional.Option;
import net.noneuclideangirl.util.Cache;
import net.noneuclideangirl.util.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final Logger log = LogManager.getLogger(DatabaseManager.class);
    private static final String COLLECTION_NAME = "services";
    private final MongoCollection<Document> services;
    private final Cache cache = new Cache();

    private static DatabaseManager INSTANCE;

    public static DatabaseManager get() {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseManager();
        }
        return INSTANCE;
    }

    public static void load() {
        get();
    }

    private DatabaseManager() {
        var dbName = ConfigManager.getString("dbName").unwrap();
        MongoCredential cred = MongoCredential.createCredential(
                ConfigManager.getString("dbUsername").unwrap(),
                dbName,
                ConfigManager.getString("dbUserPassword").unwrap().toCharArray());

        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(cred)
                .build();

        MongoDatabase db = MongoClients.create(settings)
                                       .getDatabase(dbName);

        services = db.getCollection(COLLECTION_NAME);
        if (services.countDocuments() == 0) {
            createCollection();
        }
    }

    private void createCollection() {
        log.info("Creating unique index on \"name\"");
        services.createIndex(Document.parse("{name:1}"), new IndexOptions().unique(true));
    }

    public void createService(ServiceDescriptor service) {
        services.insertOne(service.toDoc());
    }

    /**
     * Find all services in the collection, and return them as a native Collection
     */
    public Iterable<Document> findAllServices() {
        return services.find();
    }

    public Option<ServiceDescriptor> findServiceById(String id) {
        return Option.of(cache.get("service_id_" + id,
                                   () -> services.find(Filters.eq("_id", new ObjectId(id))).first()))
                     .andThen(ServiceDescriptor::fromDoc);
    }

    public Option<ServiceDescriptor> findServiceByName(String name) {
        return Option.of(cache.get("service_name_" + name,
                         () -> services.find(Filters.eq("name", name)).first()))
                     .andThen(ServiceDescriptor::fromDoc);
    }

    // TODO: I don't think I should have made the database update ServiceMonitor directly, tbh.
    public boolean updateService(String id, Document doc) {
        List<Bson> updates = new ArrayList<>();
        Option.of(doc.getString("name")).ifSome(name -> updates.add(Updates.set("name", name)));
        Option.of(doc.getString("desc")).ifSome(desc -> updates.add(Updates.set("desc", desc)));
        if (updates.size() > 0) {
            var original = services.findOneAndUpdate(Filters.eq("_id", new ObjectId(id)), Updates.combine(updates));
            if (original == null) {
                return false;
            } else {
                Option.of(doc.getString("name")).ifSome(name -> original.append("name", name));
                Option.of(doc.getString("desc")).ifSome(desc -> original.append("desc", desc));
                ServiceMonitor.changeServiceDescriptor(id, ServiceDescriptor.fromDoc(original).unwrap());
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean deleteServiceByIdOrName(Document doc) {
        // TODO: This would be a lot nicer if I wrapped the Document class.
        return Option.of(doc.getString("id"))
              .map(id -> Filters.eq("_id", new ObjectId(id)))
              .or(Option.of(doc.getString("name")).map(name -> Filters.eq("name", name)))
              .andThen(filter -> ServiceDescriptor.fromDoc(services.findOneAndDelete(filter)))
              .map(desc -> {
                  ServiceMonitor.purgeService(desc);
                  return true;
              })
              .orElse(false);
    }

    public Option<ServiceDescriptor> findServiceByIdOrName(Document doc) {
        return Option.of(doc.getString("id"))
                     .andThen(this::findServiceById)
                     .or(Option.of(doc.getString("name")).andThen(this::findServiceByName));
    }
}
