/**
 *
 */
package org.aguibert.liberty;

import java.util.Objects;
import java.util.Random;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.bson.Document;

/**
 * @author aguibert
 */
public class Person {

    private static final Random r = new Random();

    @NotNull
    public final long id;

    @NotNull
    @Size(min = 2, max = 50)
    public final String name;

    @NotNull
    @PositiveOrZero
    public final int age;

    public Person(String name, int age) {
        this(name, age, null);
    }

    @JsonbCreator
    public Person(@JsonbProperty("name") String name,
                  @JsonbProperty("age") int age,
                  @JsonbProperty("id") Long id) {
        this.name = name;
        this.age = age;
        this.id = id == null ? r.nextLong() : id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Person))
            return false;
        Person other = (Person) obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(name, other.name) &&
               Objects.equals(age, other.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.put("name", name);
        doc.put("age", age);
        doc.put("id", id);
        return doc;
    }

    public static Person fromDocument(Document doc) {
        return new Person(doc.getString("name"), doc.getInteger("age"), doc.getLong("id"));
    }

}
