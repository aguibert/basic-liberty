package org.aguibert.liberty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.websphere.crypto.PasswordUtil;
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

    @Inject
    @ConfigProperty(name = "mongo.user", defaultValue = "sampleUser")
    String user;

    @Inject
    @ConfigProperty(name = "mongo.pass.encoded", defaultValue = "{aes}APtt+/vYxxPa0jE1rhmZue9wBm3JGqFK3JR4oJdSDGWM1wLr1ckvqkqKjSB2Voty8g==") // openliberty
    String encodedPass;

    @Produces
    public MongoClient createMongo() {
        System.out.println("@AGG creating MongoClient...");
        String creds = user + ':' + PasswordUtil.passwordDecode(encodedPass) + '@';
        String mongoServer1 = hostname + ':' + port;
        // Connection string format is:
        //  mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database.collection][?options]]
        return MongoClients.create("mongodb://" + creds + mongoServer1);
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
