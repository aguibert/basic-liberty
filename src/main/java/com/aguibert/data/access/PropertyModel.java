/**
 *
 */
package com.aguibert.data.access;

import java.lang.reflect.Field;
import java.util.Objects;

import com.aguibert.data.access.anno.Column;

/**
 * @author Andrew
 *
 */
public class PropertyModel implements Comparable<PropertyModel> {

    public final Class<?> type;
    public final String name;
    public final Field f;

    public PropertyModel(Field f) {
        this.f = f;
        if (f.getType() == int.class)
            this.type = Integer.class; // TODO: cannot decode value of type 'int' --> possible bug in r2dbc?
        else
            this.type = f.getType();
        Column anno = f.getAnnotation(Column.class);
        if (anno != null)
            this.name = anno.name();
        else
            this.name = f.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PropertyModel))
            return false;
        PropertyModel other = (PropertyModel) obj;
        return Objects.equals(type, other.type) &&
               Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public int compareTo(PropertyModel o) {
        return name.compareTo(o.name);
    }

}
