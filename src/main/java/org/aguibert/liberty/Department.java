/**
 *
 */
package org.aguibert.liberty;

import com.aguibert.data.access.anno.Id;

public class Department {

    @Id
    public int id;

    public String name;

    public Department() {}

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
