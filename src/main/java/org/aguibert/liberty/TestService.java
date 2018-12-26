package org.aguibert.liberty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.aguibert.data.access.DataAccessTemplate;
import com.aguibert.data.access.RegisterDataTemplate;
import com.aguibert.data.access.anno.Select;
import com.aguibert.data.access.anno.Update;

import io.r2dbc.client.R2dbc;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Path("/")
@ApplicationScoped
public class TestService {

    @RegisterDataTemplate
    public static interface PersonRepo extends DataAccessTemplate<Integer, Person> {
        @Select("SELECT * FROM Person WHERE theName like $1")
        Flux<Person> findByName(String name);

        @Update("UPDATE ...")
        Mono<Long> updatePersonByName(String name);
    }

    @Inject
    PersonRepo personRepo;

    @RegisterDataTemplate
    public static interface DeptRepo extends DataAccessTemplate<Integer, Department> {}

    @Inject
    DeptRepo deptRepo;

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
//        log("Hello world");
//        log("Using loader: " + getClass().getClassLoader());
//        DB db = new DB();
//        Statement stmt = db.create().createStatement();
//        try {
//            stmt.execute("CREATE TABLE PERSON (id int primary key, name varchar(255))");
//            log("Created new table");
//        } catch (SQLException e) {
//            log("Table already existed UPDATED6 ...");
//        }
//        stmt.execute("DELETE FROM PERSON");

        log("personRepo is: " + personRepo);

        personRepo.getDbc()
                        .inTransaction(h -> h.execute("DROP TABLE IF EXISTS Person"))
                        .blockFirst();
        deptRepo.getDbc()
                        .inTransaction(h -> h.execute("DROP TABLE IF EXISTS Department"))
                        .blockFirst();
        personRepo.getDbc()
                        .inTransaction(h -> h.execute("CREATE TABLE IF NOT EXISTS Person (id int primary key, theName varchar(255), orgId int)"))
                        .blockFirst();
        deptRepo.getDbc()
                        .inTransaction(h -> h.execute("CREATE TABLE IF NOT EXISTS Department (id int primary key, name varchar(255))"))
                        .blockFirst();
        log("Created tables");

        deptRepo.insert(new Department(1, "WebSphere"));

        personRepo.insert(new Person(1, 1, "Andy")).subscribe();
        personRepo.insert(new Person(2, 1, "Bob"))
                        .subscribe(i -> log("@AGG sub bob: " + i));
        personRepo.insert(new Person(3, 1, "Chuck")).subscribe();

//        stmt.execute("INSERT INTO PERSON(id, name) VALUES(1, 'Anju')");
//        stmt.execute("INSERT INTO PERSON(id, name) VALUES(2, 'Sonia')");
//        stmt.execute("INSERT INTO PERSON(id, name) VALUES(3, 'Asha')");

        log("<h2>Found R2DBC results:</h2>");
        personRepo.findBy(1)
                        .subscribe(p -> log("FOUND PERSON: " + p.name));
        personRepo.findBy(2)
                        .subscribe(p -> log("FOUND PERSON: " + p.name));
        personRepo.findBy(3)
                        .subscribe(p -> log("FOUND PERSON: " + p.name));

        deptRepo.findBy(1)
                        .subscribe(d -> log("FOUND DEPT: " + d.name));

        personRepo.findByName("Andy")
                        .subscribe(p -> log("CUSTOM QUERY found: " + p.name));

        Statement stmt = create().createStatement();
        ResultSet rs = stmt.executeQuery("select * from Person");
        log("<h2>Found JDBC results:</h2>");
        log("<ul>");
        while (rs.next()) {
            log("<li> Id " + rs.getInt("id") + " Name " + rs.getString("theName") + "</li>");
        }
        log("</ul>");

        H2ConnectionConfiguration h2Config = H2ConnectionConfiguration.builder()
                        .inMemory("test")
                        .build();
        R2dbc r2dbc = new R2dbc(new H2ConnectionFactory(h2Config));
//        r2dbc.inTransaction(handle -> handle.execute("INSERT INTO test VALUES ($1)", 100)) // Flux<Integer>
//                        .thenMany(
//                                  r2dbc.inTransaction(handle -> handle.select("SELECT value FROM test")
//                                                  .mapResult(result -> result.map((row, rowMetadata) -> row.get("value", Integer.class)))))
//                        .subscribe(System.out::println);
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg);
        sb.append("<br/>");
    }

    public Connection create() throws SQLException, ClassNotFoundException {
        System.out.println("Found class: " + Class.forName("org.h2.Driver"));
        Connection con = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
        System.out.println("Got connection: " + con);
        return con;
    }

    @Produces
    public ConnectionFactory createConfig() {
        // jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
        H2ConnectionConfiguration config = H2ConnectionConfiguration.builder()
                        .inMemory("test")
                        .build();
        return new H2ConnectionFactory(config);
    }

}
