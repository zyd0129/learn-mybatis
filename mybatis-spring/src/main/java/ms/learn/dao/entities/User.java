package ms.learn.dao.entities;

import lombok.Data;

@Data
public class User {
    private int id;
    private String name;
    private String pwd;
    private Teacher teacher;
}
