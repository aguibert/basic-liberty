package org.aguibert.cdi.timers;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ScheduleExtension implements Extension {

    private final ManagedScheduledExecutorService exec;
    private final Set<Schedule> schedules = new HashSet<>();
    private final Set<ScheduledFuture<?>> runningTasks = new HashSet<>();

    public ScheduleExtension() throws NamingException {
        System.out.println("@AGG extension registered");
        exec = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
    }

    public void collectSchedules(@Observes ProcessObserverMethod<Instant, ?> observer) {
        System.out.println("@AGG process observer method=" + observer.getObserverMethod());
        Schedule s = getSchedule(observer.getObserverMethod().getObservedQualifiers());
        if (s != null) {
            System.out.println(" adding method");
            schedules.add(s);
        }
    }

    public void startSchedules(@Observes @Initialized(ApplicationScoped.class) Object o, BeanManager bm) {
        System.out.println("@AGG starting timers");
        for (Schedule anno : schedules) {
            System.out.println("@AGG start timer for " + anno);
            ScheduledFuture<?> task = exec.scheduleAtFixedRate(() -> {
                System.out.println("Fire tasks...");
                bm.getEvent().select(Instant.class, anno).fire(Instant.now());
            }, 0, anno.interval(), anno.unit());
            runningTasks.add(task);
        }
    }

    public void stopSchedules(@Observes @Destroyed(ApplicationScoped.class) Object o) {
        System.out.println("@AGG stopping timers");
        runningTasks.stream().forEach(task -> task.cancel(false));
    }

    private Schedule getSchedule(Set<Annotation> annos) {
        for (Annotation a : annos) {
            if (a instanceof Schedule) {
                return (Schedule) a;
            }
        }
        return null;
    }

}
