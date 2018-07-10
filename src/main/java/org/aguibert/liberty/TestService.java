package org.aguibert.liberty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

@Path("/")
@ApplicationScoped
public class TestService {

    @Inject
    MongoDatabase db;

    private static final Random r = new Random();

//    @Resource(lookup = "mongo/myDB")
//    com.mongodb.MongoClient client;

    @GET
    public String test() {
        try {
            log(">>> ENTER");
            doTest();
            log("<<< EXIT SUCCESSFUL");
        } catch (Exception e) {
            e.printStackTrace(System.out);
            e.printStackTrace(new PrintWriter(sb));
            log("<<< EXIT FAILED");
        }
        String result = sb.toString();
        sb = new StringWriter();
        return result;
    }

    private void doTest() throws Exception {
        log("got db: " + db);

        MongoCollection<Document> customers = db.getCollection("customers");
        Document bob = new Document();
        bob.put("name", "Bob-" + r.nextInt(99));
        bob.put("age", 42);
        customers.insertOne(bob);
        log("Inserted: " + bob);

        // Select all people with age <=50 and increment age by 1
        Document query = new Document("age", new Document("$lte", 50));
        Document update = new Document("$inc", new Document("age", 1));
        UpdateResult result = customers.updateMany(query, update);
        log(">>> UPDATE matched " + result.getMatchedCount() + " items and updated " + result.getModifiedCount() + " of them");

        log("After bulk update:");
        for (Document person : customers.find())
            log("  Found person: " + person);
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg);
        sb.append("<br/>");
    }

}
