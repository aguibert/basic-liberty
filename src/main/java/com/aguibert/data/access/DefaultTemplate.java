/**
 *
 */
package com.aguibert.data.access;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.CDI;

import com.aguibert.data.access.anno.Select;
import com.aguibert.data.access.anno.Update;

import io.r2dbc.client.Handle;
import io.r2dbc.client.R2dbc;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Andrew
 */
public class DefaultTemplate<K, T> implements DataAccessTemplate<K, T>, InvocationHandler {

    private final ConnectionFactory cf;
    private final R2dbc dbc;
    private final Class<K> keyType;
    private final Class<T> dataType;
    private final ClassModel<T> classModel;

    private String tableName;

    public DefaultTemplate(Class<K> key, Class<T> dataType) {
        cf = CDI.current().select(ConnectionFactory.class, Default.Literal.INSTANCE).get();
        dbc = new R2dbc(cf);
        this.keyType = key;
        this.dataType = dataType;
        System.out.println("keyType=" + keyType);
        System.out.println("dataType=" + dataType);
        this.tableName = dataType.getSimpleName();
        this.classModel = new ClassModel<T>(dataType);
    }

    @Override
    public R2dbc getDbc() {
        return dbc;
    }

    private String getTableName() {
        return tableName;
    }

    @Override
    public boolean createDB(String name) {
        this.tableName = name;
        // TODO table schemas
        long res = dbc.inTransaction(h -> h.execute("CREATE TABLE " + name + " (id int primary key, name varchar(255))"))
                        .blockFirst();
        System.out.println("Created with result: " + res);
        return res == 0;
    }

    @Override
    public boolean autoCreateDB() {
        String dbName = dataType.getSimpleName();
        System.out.println("Using DBName: " + dbName);
        try {
            return createDB(dbName);
        } catch (Exception e) {
            System.out.println("DB already exists");
            return false;
        }
    }

    @Override
    public Mono<Boolean> contains(T item) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Boolean> containsKey(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flux<T> findBy(K key) {
        System.out.println("TEMPLATE: findBy " + key.toString());
        return dbc.inTransaction(h -> h.select("SELECT * FROM " + getTableName() + " WHERE id = ($1)", key)
                        .mapRow((row, metaData) -> classModel.create(row, metaData)));
    }

    @Override
    public Flux<Integer> insert(T item) {
        System.out.println("TEMPLATE: insert " + item.toString());
        return dbc.inTransaction(h -> createQuery(h, item))
                        .doOnEach((i) -> System.out.println("TEMPLATE: doOnEach inserted " + i))
                        .doOnNext((i) -> System.out.println("TEMPLATE: doOnNext inserted " + i));
    }

    private Flux<Integer> createQuery(Handle h, T item) {
        // TODO generate query string and param values together to account for null values
        String query = "INSERT INTO " + getTableName() + classModel.generateQueryValueTemplate();
        Object[] params = classModel.getQueryParameters(item);
        return h.execute(query, params);
    }

    @Override
    public Mono<Boolean> insert(K key, T item) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Long> remove(T item) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flux<Boolean> removeBy(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Long> update(T item, T updatedItem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Boolean> updateBy(K key, T updatedItem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        System.out.println("@AGG inside invoke method=" + name);

        if (name.equals("toString"))
            return this.toString();
        if (name.equals("insert") && args.length == 1)
            return this.insert((T) args[0]);
        if (name.equals("insert") && args.length == 2)
            return this.insert((K) args[0], (T) args[1]);
        if (name.equals("remove"))
            return this.remove((T) args[0]);
        if (name.equals("findBy"))
            return this.findBy((K) args[0]);
        if (name.equals("getDbc"))
            return this.getDbc();

        System.out.println("  proxy=" + proxy);
        System.out.println("  method=" + method.getName());
        System.out.println("  args=" + Arrays.toString(args));

        Select stmtAnno = method.getAnnotation(Select.class);
        if (stmtAnno != null) {
            return dbc.inTransaction(h -> h.select(stmtAnno.value(), args[0])
                            .mapRow((row, metaData) -> classModel.create(row, metaData)));
        }

        Update updateAnno = method.getAnnotation(Update.class);
        if (updateAnno != null) {
            return dbc.inTransaction(h -> h.execute(updateAnno.value(), args[0]));
        }

        return null;
    }

}
