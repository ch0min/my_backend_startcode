package entities;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQuery(name = "User.deleteAllRows", query = "DELETE from User")
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @Column(name = "user_name", length = 25)
    private String userName;

    @NotNull
    @Column(name = "user_email")
    private String userEmail;

    @NotNull
    @Column(name = "user_pass")
    private String userPass;

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_name"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    private List<Role> roles = new ArrayList<>();

    public User() {
    }

    public User(String userName, String userPass) {
        this.userName = userName;
        this.userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
    }

    public User(String userName, String userPass, List<Role> roles) {
        this.userName = userName;
        this.userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
        this.roles = roles;
    }

    public User(String userName, String userEmail, String userPass) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
    }

    public User(String userName, String userEmail, String userPass, List<Role> roles) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
        this.roles = roles;
    }


    public List<String> getRolesAsStrings() {
        if (roles.isEmpty()) {
            return null;
        }
        List<String> rolesAsStrings = new ArrayList<>();
        roles.forEach((role) -> {
            rolesAsStrings.add(role.getRoleName());
        });
        return rolesAsStrings;
    }

    public boolean verifyPassword(String pw) {
        return (BCrypt.checkpw(pw, userPass));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPass() {
        return userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
    }

    public void setUserPass(String userPass) {
        this.userPass = BCrypt.hashpw(userPass, BCrypt.gensalt());
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role userRole) {
        roles.add(userRole);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getUserName().equals(user.getUserName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserName());
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPass='" + userPass + '\'' +
                ", roles=" + roles +
                '}';
    }
}
