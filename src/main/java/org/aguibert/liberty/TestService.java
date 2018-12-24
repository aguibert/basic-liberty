package org.aguibert.liberty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.r2dbc.client.R2dbc;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;

@Path("/")
@ApplicationScoped
public class TestService {

    @Inject
    Connection con;

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
        log("Using loader: " + getClass().getClassLoader());
        log("Using connection: " + con);
        Statement stmt = con.createStatement();
        try {
            stmt.execute("CREATE TABLE PERSON (id int primary key, name varchar(255))");
            log("Created new table");
        } catch (SQLException e) {
            log("Table already existed UPDATED6 ...");
        }

        stmt.execute("DELETE FROM PERSON");

        stmt.execute("INSERT INTO PERSON(id, name) VALUES(1, 'Anju')");
        stmt.execute("INSERT INTO PERSON(id, name) VALUES(2, 'Sonia')");
        stmt.execute("INSERT INTO PERSON(id, name) VALUES(3, 'Asha')");

        ResultSet rs = stmt.executeQuery("select * from PERSON");
        log("<h2>Found results:</h2>");
        log("<ul>");
        while (rs.next()) {
            log("<li> Id " + rs.getInt("id") + " Name " + rs.getString("name") + "</li>");
        }
        log("</ul>");

        H2ConnectionConfiguration h2Config = H2ConnectionConfiguration.builder()
                        .inMemory("test")
                        .build();
        R2dbc r2dbc = new R2dbc(new H2ConnectionFactory(h2Config));
        r2dbc.inTransaction(handle -> handle.execute("INSERT INTO test VALUES ($1)", 100)) // Flux<Integer>
                        .thenMany(
                                  r2dbc.inTransaction(handle -> handle.select("SELECT value FROM test")
                                                  .mapResult(result -> result.map((row, rowMetadata) -> row.get("value", Integer.class)))))
                        .subscribe(System.out::println);
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg);
        sb.append("<br/>");
    }

}
