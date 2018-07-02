package org.aguibert.liberty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class MongoProducer {

    @Inject
    @ConfigProperty(name = "mongo.hostname", defaultValue = "localhost")
    String hostname;

    @Inject
    @ConfigProperty(name = "mongo.port", defaultValue = "27017")
    int port;

    @Inject
    @ConfigProperty(name = "mongo.dbname", defaultValue = "testdb")
    String dbName;

    @Produces
    public MongoClient createMongo() {
        System.out.println("@AGG creating MongoClient...");
//        MongoClients.create(MongoClientSettings.builder()
//                            .applyToConnectionPoolSettings(() -> {
//
//                            }));
        return MongoClients.create("mongodb://" + hostname + ':' + port);
    }

    @Produces
    public MongoDatabase createDB(MongoClient client) {
        System.out.println("@AGG creating MongoDatabase...");
        return client.getDatabase(dbName);
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }

}
