package datafacades;

import entities.Role;
import entities.User;
import errorhandling.API_Exception;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserFacadeTest {
    private static EntityManagerFactory emf;
    private static UserFacade facade;

    Role userRole, adminRole;
    User user, admin;

    public UserFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = UserFacade.getUserFacade(emf);
    }

    @AfterAll
    public static void tearDownClass() {
        System.out.println("EXECUTION OF ALL TESTS IN USERFACADETEST DONE");
    }

    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.createNamedQuery("User.deleteAllRows").executeUpdate();
            em.createNamedQuery("Role.deleteAllRows").executeUpdate();

            userRole = new Role("user");
            adminRole = new Role("admin");

            user = new User("user", "user@gmail.com", "test123");
            admin = new User("admin", "admin@gmail.com", "test123");

            // Adding
            admin.addRole(adminRole);
            user.addRole(userRole);

            // Persisting
            em.persist(userRole);
            em.persist(adminRole);

            em.persist(user);
            em.persist(admin);

            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    @AfterEach
    public void tearDown() {
        System.out.println("EXECUTION OF TEST DONE");
    }

    @Test
    void getAllUsersTest() throws API_Exception {
        List<User> actual = facade.getAllUsers();
        int expected = 2;
        assertEquals(expected, actual.size());
    }

    @Test
    void getUserByUsernameTest() throws API_Exception {
        User testUser = facade.getUserByUserName(user.getUserName());
        assertEquals(user, testUser);
    }

    @Test
    void createUserTest() throws API_Exception {
        User user = new User("Fido", "test123");
        facade.createUser(user);
        assertNotNull(user.getUserName());
        int actualSize = facade.getAllUsers().size();
        assertEquals(3, actualSize);
    }

    @Test
    void createNoDuplicateUsersTest() throws API_Exception {
        User user = new User("user", "test123");
        assertThrows(API_Exception.class, () -> facade.createUser(user));
    }

    @Test
    void updateUserTest() throws API_Exception {
        User expected = new User(user.getUserName(), "Testefar@test.com", "test123");
        User actual = facade.updateUser(expected);
        assertEquals(expected, actual);
    }

    @Test
    void deleteUserTest() throws API_Exception {
        facade.deleteUser("user");
        int actualSize = facade.getAllUsers().size();
        assertEquals(1, actualSize);
    }

    @Test
    void cantFindUserToDeleteTest() {
        assertThrows(API_Exception.class, () -> facade.deleteUser("TestBruger"));
    }
}
