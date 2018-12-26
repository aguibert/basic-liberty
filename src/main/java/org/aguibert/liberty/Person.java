/**
 *
 */
package org.aguibert.liberty;

import com.aguibert.data.access.anno.Column;
import com.aguibert.data.access.anno.Id;

/**
 * @author Andrew
 *
 */
public class Person {

    @Id
    public int id;

    public int orgId;

    @Column(name = "theName")
    public String name;

    public Person() {}

    public Person(int id, int orgId, String name) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
    }

}
