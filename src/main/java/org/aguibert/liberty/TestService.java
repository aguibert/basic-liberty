package org.aguibert.liberty;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
@ApplicationScoped
public class TestService {

    @Resource(lookup = "jdbc/h2test")
    DataSource ds;

    @PersistenceContext(unitName = "h2test")
    EntityManager em;

    @Resource
    UserTransaction tx;

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
        ds.getConnection().close();
        log("Got a connection");

        tx.begin();
        em.persist(new Book(1, "Jon", "Some Book", 10));
        Book found = em.find(Book.class, 1);
        log("Found book: " + found);
        tx.commit();
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg.replace("\n", "<br>"));
        sb.append("<br/>");
    }

}
