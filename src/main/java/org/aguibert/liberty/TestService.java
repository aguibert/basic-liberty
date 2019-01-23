package org.aguibert.liberty;

import java.io.StringWriter;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
@RequestScoped
public class TestService {

    @GET
    public String test() {
        log("Hello world");
        log("The java runtime is version:" + System.getProperty("java.specification.version"));
        return sb.toString();
    }

    private StringWriter sb = new StringWriter();

    private void log(String msg) {
        System.out.println(msg);
        sb.append(msg);
        sb.append("<br/>");
    }

}
