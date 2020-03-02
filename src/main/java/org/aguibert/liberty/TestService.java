package org.aguibert.liberty;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.ws.rs.GET;

import org.aguibert.cdi.timers.Schedule;

@ApplicationScoped
public class TestService {

    @GET
    public String sayHello() {
        return "Hello world";
    }

    public void onScheduleA1(@Observes @Schedule(interval = 3, unit = TimeUnit.SECONDS) Instant now) {
        System.out.println("@AGG onSchedule a1  now=" + now);
    }

    public void onScheduleA2(@Observes @Schedule(interval = 3, unit = TimeUnit.SECONDS) Instant now) {
        System.out.println("@AGG onSchedule a2  now=" + now);
    }

    public void onSchedule2(@Observes @Schedule(interval = 9, unit = TimeUnit.SECONDS) Instant now) {
        System.out.println("@AGG onSchedule b1  now=" + now);
    }

}
