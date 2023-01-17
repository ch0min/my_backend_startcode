package entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQuery(name = "Role.deleteAllRows", query = "DELETE from Role")
@Table(name = "roles")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @Column(name = "role_name", length = 20)
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private List<User> userList;

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return getRoleName().equals(role.getRoleName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleName());
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleName='" + roleName + '\'' +
                ", userList=" + userList +
                '}';
    }
}
