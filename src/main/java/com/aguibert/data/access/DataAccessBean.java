/**
 *
 */
package com.aguibert.data.access;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

/**
 * @author Andrew
 *
 */
public class DataAccessBean<K, T> implements Bean<DataAccessTemplate<K, T>>, PassivationCapable {

    private final Class<? extends DataAccessTemplate<K, T>> ifc;
    private final BeanManager bm;

    public DataAccessBean(Class<? extends DataAccessTemplate<K, T>> ifc, BeanManager bm) {
        this.ifc = ifc;
        this.bm = bm;
    }

    @Override
    public DataAccessTemplate<K, T> create(CreationalContext<DataAccessTemplate<K, T>> creationalContext) {
        Class<K> keyType = (Class<K>) ((ParameterizedType) ifc.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        Class<T> dataType = (Class<T>) ((ParameterizedType) ifc.getGenericInterfaces()[0]).getActualTypeArguments()[1];
        System.out.println("keyType=" + keyType);
        System.out.println("dataType=" + dataType);
        Class<?>[] ifcs = new Class[] { ifc };
        return (DataAccessTemplate<K, T>) Proxy.newProxyInstance(getClass().getClassLoader(), ifcs, new DefaultTemplate<K, T>(keyType, dataType));
    }

    @Override
    public void destroy(DataAccessTemplate<K, T> instance, CreationalContext<DataAccessTemplate<K, T>> creationalContext) {
        creationalContext.release();
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(ifc);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.singleton(Default.Literal.INSTANCE);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return ifc.getName();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return ifc.getName();
    }

    @Override
    public Class<?> getBeanClass() {
        return ifc;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }
}
