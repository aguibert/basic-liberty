/**
 *
 */
package com.aguibert.data.access;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.aguibert.data.access.anno.Id;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

/**
 * @author Andrew
 *
 */
public class ClassModel<T> {

    public final Class<T> clazz;

    private PropertyModel idField;
    private final Set<PropertyModel> columns = new TreeSet<>();

    public ClassModel(Class<T> clazz) {
        this.clazz = clazz;

        for (Field f : clazz.getFields()) {
            PropertyModel p = new PropertyModel(f);
            if (f.isAnnotationPresent(Id.class))
                idField = p;
            columns.add(p);
        }
    }

    public String getTableName() {
        // possibly override with @Table annotation
        return clazz.getSimpleName();
    }

    public Optional<PropertyModel> getId() {
        return Optional.ofNullable(idField);
    }

    public Set<PropertyModel> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public T create(Row row, RowMetadata metaData) {
        try {
            T instance = clazz.newInstance();
            for (PropertyModel prop : getColumns())
                prop.f.set(instance, row.get(prop.name, prop.type));
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateQueryValueTemplate() {
        StringBuilder propNames = new StringBuilder();
        StringBuilder propValues = new StringBuilder();
        Iterator<PropertyModel> iter = columns.iterator();
        int i = 1;
        while (iter.hasNext()) {
            PropertyModel prop = iter.next();
            propNames.append(prop.name);
            propValues.append("($").append(i).append(")");
            if (iter.hasNext()) {
                propNames.append(',');
                propValues.append(',');
            }
            i++;
        }
        return new StringBuilder("(").append(propNames).append(") VALUES(").append(propValues).append(")").toString();
    }

    public Object[] getQueryParameters(T instance) {
        Object[] params = new Object[columns.size()];
        int i = 0;
        for (PropertyModel prop : columns) {
            try {
                params[i] = prop.f.get(instance);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                i++;
            }
        }
        return params;
    }

}
