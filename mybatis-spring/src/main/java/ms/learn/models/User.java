package ms.learn.models;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import ms.learn.dao.entities.Teacher;

import javax.validation.constraints.NotBlank;

@Data
public class User {
    public interface UserSimpleView {
    }


    public interface UserDetailView extends UserSimpleView {
    }


    @JsonView(UserSimpleView.class)
    private int id;
    @JsonView(UserSimpleView.class)
    private String name;
    @JsonView(UserDetailView.class)
    @NotBlank
    private String pwd;
    @JsonView(UserDetailView.class)
    private Teacher teacher;
}
