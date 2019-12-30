package net.noneuclideangirl.util;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import net.noneuclideangirl.data.ServiceDescriptor;
import net.noneuclideangirl.functional.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

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

    public Option<Document> findServiceById(String id) {
        return Option.of(cache.get("service_id_" + id,
                                   () -> services.find(Filters.eq("_id", new ObjectId(id))).first()));
    }

    public Option<Document> findServiceByName(String name) {
        return Option.of(cache.get("service_name_" + name,
                         () -> services.find(Filters.eq("name", name)).first()));
    }

    public Option<Document> findServiceByIdOrName(Document doc) {
        Option<Document> service = Option.none();
        if (doc.containsKey("id")) {
            service = findServiceById(doc.getString("id"));
        }
        if (doc.containsKey("name")) {
            service = findServiceByName(doc.getString("name"));
        }
        return service;
    }
}
