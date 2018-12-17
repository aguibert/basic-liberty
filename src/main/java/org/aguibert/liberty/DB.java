/**
 *
 */
package org.aguibert.liberty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * @author Andrew
 *
 */
@ApplicationScoped
public class DB {

    @Produces
    @RequestScoped
    public Connection create() throws SQLException, ClassNotFoundException {
        System.out.println("Found class: " + Class.forName("org.h2.Driver"));
        Connection con = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
        System.out.println("Got connection: " + con);
        return con;
    }

    public void close(@Disposes Connection con) throws SQLException {
        System.out.println("Closing connection: " + con);
        con.close();
    }

}
