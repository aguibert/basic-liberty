package org.aguibert.liberty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class MongoProducer {

    @Inject
    @ConfigProperty(name = "PERSON_REPO_NAME", defaultValue = "People")
    String PERSON_REPO_NAME;

    @Inject
    @ConfigProperty(name = "MONGO_HOSTNAME", defaultValue = "127.0.0.1")
    String MONGO_HOSTNAME;

    @Inject
    @ConfigProperty(name = "MONGO_PORT", defaultValue = "27017")
    int MONGO_PORT;

    @Produces
    public MongoCollection<Document> getPersonRepo(MongoDatabase db) {
        return db.getCollection(PERSON_REPO_NAME);
    }

    @Produces
    public MongoClient createMongo() {
        System.out.println("Create mongo with host=" + MONGO_HOSTNAME + " port=" + MONGO_PORT);
        return new MongoClient(new ServerAddress(MONGO_HOSTNAME, MONGO_PORT), //
                        new MongoClientOptions.Builder()
                                        .connectTimeout(5000)
                                        .maxWaitTime(5000)
                                        .build());
    }

    @Produces
    public MongoDatabase createDB(MongoClient client) {
        return client.getDatabase("testdb");
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }
}