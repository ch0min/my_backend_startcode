package datafacades;

import entities.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.text.ParseException;

public class Populator {
    public static void populate() throws ParseException {
        EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();

        Role userRole = new Role("user");
        Role adminRole = new Role("admin");

        User user = new User("user", "user@gmail.com","test123");
        User admin = new User("admin", "admin@gmail.com","test123");
        User mark = new User("mark", "fido@gmail.com","test123");

        if(admin.getUserPass().equals("test")||user.getUserPass().equals("test"))
            throw new UnsupportedOperationException("You have not changed the passwords");

        em.getTransaction().begin();
        em.persist(userRole);
        em.persist(adminRole);

        em.persist(user);
        em.persist(admin);
        em.persist(mark);

        admin.addRole(adminRole);
        user.addRole(userRole);
        mark.addRole(adminRole);

        em.getTransaction().commit();
        System.out.println("PW: " + user.getUserPass());
        System.out.println("Testing user with OK password: " + user.verifyPassword("test"));
        System.out.println("Testing user with wrong password: " + user.verifyPassword("test1"));
        System.out.println("Created TEST Users");

    }

    public static void main(String[] args) throws ParseException {
        populate();
    }
}
