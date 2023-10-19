package rabbit.flt.test.common.bean;

import javax.persistence.Column;
import javax.persistence.Id;

public class User {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_name")
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
