/**
 *
 */
package com.aguibert.data.access;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

/**
 * @author Andrew
 *
 */
public class DataAccessExtension implements Extension {

    private final Set<Class<?>> templateClasses = new HashSet<>();

    public DataAccessExtension() {
        System.out.println("@AGG create extension");
    }

    public void findTemplates(@Observes @WithAnnotations(RegisterDataTemplate.class) ProcessAnnotatedType<?> event) {
        Class<?> dataTemplate = event.getAnnotatedType().getJavaClass();
        System.out.println("@AGG found data template: " + dataTemplate);
        templateClasses.add(dataTemplate);
    }

    public void registerTemplates(@Observes AfterBeanDiscovery event, BeanManager bm) {
        System.out.println("after bean discovery");
        for (Class<?> templateClass : templateClasses) {
            System.out.println("@AGG register data template: " + templateClass);
            event.addBean(new DataAccessBean(templateClass, bm));
        }
    }

}
