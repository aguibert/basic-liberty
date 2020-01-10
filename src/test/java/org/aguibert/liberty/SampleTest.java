package org.aguibert.liberty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

@MicroShedTest
public class SampleTest {

    @Container
    public static ApplicationContainer app = new ApplicationContainer()
                    .withAppContextRoot("/basic-liberty")
                    .withExposedPorts(8080);

    @RESTClient
    public static TestService testSvc;

    @Test
    public void testHello() {
        assertEquals("Hello world", testSvc.sayHello());
    }

}
