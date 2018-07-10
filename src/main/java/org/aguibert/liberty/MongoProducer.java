package org.aguibert.liberty;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.websphere.crypto.PasswordUtil;
import com.ibm.websphere.ssl.JSSEHelper;
import com.ibm.websphere.ssl.SSLException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
    public MongoClient createMongo() throws SSLException {
        System.out.println("creating MongoClient...");
        MongoCredential creds = MongoCredential.createCredential(user, dbName, PasswordUtil.passwordDecode(encodedPass).toCharArray());
        SSLContext sslContext = JSSEHelper.getInstance().getSSLContext("mySSL", Collections.emptyMap(), null);
        return new MongoClient(new ServerAddress(), creds, new MongoClientOptions.Builder()
                        .sslEnabled(true)
                        .sslContext(sslContext)
                        .sslInvalidHostNameAllowed(true) // ignore cert validation for local experimenting
                        .build());
    }

    @Produces
    public MongoDatabase createDB(MongoClient client) {
        System.out.println("creating MongoDatabase...");
        return client.getDatabase(dbName);
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }
}
