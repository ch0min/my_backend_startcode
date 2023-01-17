package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.UserDTO;
import entities.Role;
import entities.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

//Disabled
public class LoginEndpointTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    Role userRole, adminRole;
    User user, admin, user_admin;

    UserDTO udto, udtoA, udtoBoth;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;
    
    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
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
            user_admin = new User("user_admin", "user_admin@gmail.com", "test123");

            // Adding
            admin.addRole(adminRole);
            user.addRole(userRole);
            user_admin.addRole(userRole);
            user_admin.addRole(adminRole);

            // Persisting
            em.persist(userRole);
            em.persist(adminRole);

            em.persist(user);
            em.persist(admin);
            em.persist(user_admin);

            em.getTransaction().commit();

        } finally {
            udto = new UserDTO(user);
            udtoA = new UserDTO(admin);
            udtoBoth = new UserDTO(user_admin);

            em.close();
        }
    }

    // This is how we hold on to the token after login, similar to that a client must store the token somewhere
    private static String securityToken;

    //Utility method to login and set the returned securityToken
    private static void login(String role, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", role, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                //.when().post("/api/login")
                .when().post("/login")
                .then()
                .extract().path("token");
        //System.out.println("TOKEN ---> " + securityToken);
    }

    private void logOut() {
        securityToken = null;
    }

    @Test
    public void serverIsRunning() {
        given().when().get("/info").then().statusCode(200);
    }

    @Test
    public void testRestNoAuthenticationRequired() {
        given()
                .contentType("application/json")
                .when()
                .get("/info/").then()
                .statusCode(200)
                .body("msg", equalTo("Hello anonymous"));
    }

    @Test
    public void testRestForAdmin() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .get("/info/admin").then()
                .statusCode(200)
                .body("msg", equalTo("Hello to (admin) User: admin"));
    }

    @Test
    public void testRestForUser() {
        login("user", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/info/user").then()
                .statusCode(200)
                .body("msg", equalTo("Hello to User: user"));
    }

    @Test
    public void testAutorizedUserCannotAccessAdminPage() {
        login("user", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/info/admin").then() //Call Admin endpoint as user
                .statusCode(401);
    }

    @Test
    public void testAutorizedAdminCannotAccessUserPage() {
        login("admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/info/user").then() //Call User endpoint as Admin
                .statusCode(401);
    }

    @Test
    public void testRestForMultiRole1() {
        login("user_admin", "test123");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .get("/info/admin").then()
                .statusCode(200)
                .body("msg", equalTo("Hello to (admin) User: user_admin"));
    }

    @Test
    public void testRestForMultiRole2() {
        login("user_admin", "test123");
        given()
                .contentType("application/json")
                .header("x-access-token", securityToken)
                .when()
                .get("/info/user").then()
                .statusCode(200)
                .body("msg", equalTo("Hello to User: user_admin"));
    }

    @Test
    public void userNotAuthenticated() {
        logOut();
        given()
                .contentType("application/json")
                .when()
                .get("/info/user").then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Not authenticated - do login"));
    }

    @Test
    public void adminNotAuthenticated() {
        logOut();
        given()
                .contentType("application/json")
                .when()
                .get("/info/user").then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Not authenticated - do login"));
    }

}
