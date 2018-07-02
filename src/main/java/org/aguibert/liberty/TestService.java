package org.aguibert.liberty;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.mongodb.client.MongoDatabase;

@Path("/")
@ApplicationScoped
public class TestService {

    @Inject
    MongoDatabase db;

    @Resource(lookup = "mongo/myDB")
    com.mongodb.MongoClient client;

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
        log("Hello world");
        log("@AGG got client: " + client);
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg);
        sb.append("<br/>");
    }

}
