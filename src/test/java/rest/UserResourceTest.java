package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.UserDTO;
import entities.Role;
import entities.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
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
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class UserResourceTest {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    Role userRole, adminRole;
    User user, admin;

    UserDTO udto, udtoA;

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
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        httpServer = startServer();
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
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
            udto = new UserDTO(user);
            udtoA = new UserDTO(admin);

            em.close();
        }
    }

    @Test
    public void testServerIsUp() {
        System.out.println("Testing is server UP");
        given().when().get("/users/all").then().statusCode(200);
    }

    @Test
    public void testLogRequest() {
        System.out.println("Testing logging request details");
        given().log().all()
                .when().get("/users/all")
                .then().statusCode(200);
    }

    @Test
    public void testLogResponse() {
        System.out.println("Testing logging response details");
        given()
                .when().get("/users/all")
                .then().log().body().statusCode(200);
    }

    @Test
    public void testPrintResponse() {
        Response response = given().when().get("/users/" + udto.getUserName());
        ResponseBody body = response.getBody();
        System.out.println(body.prettyPrint());
        response
                .then()
                .assertThat()
                .body("userName", equalTo("user"));
    }

    @Test
    void getAllUsersTest() {
        List<UserDTO> usersDTOS;
        usersDTOS = given()
                .contentType("application/json")
                .when()
                .get("/users/all")
                .then()
                .extract().body().jsonPath().getList("", UserDTO.class);
        assertThat(usersDTOS, containsInAnyOrder(udto, udtoA));
    }

    @Test
    void getUserByUserNameTest() {
        given()
                .contentType(ContentType.JSON)
                .get("/users/" + udto.getUserName())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("userName", equalTo(udto.getUserName()))
                .body("roles", containsInAnyOrder(udto.getRoles().get(0)));
    }

    @Test
    void createUserTest() {
        User user = new User("Perle", "test123", "perle@gmail.com");
        Role role = new Role("user");
        user.addRole(role);

        UserDTO udto = new UserDTO(user);
        String requestBody = GSON.toJson(udto);

        given()
                .header("Content-type", ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .assertThat()
                .statusCode(200)
                .body("userName", equalTo("Perle"))
                .body("roles", containsInAnyOrder("user"));
    }

    @Test
    void updateUserTest() {
        udto.setUserEmail("nytestemail@gmail.com");
        udto.setRoles(new ArrayList<>());
        given()
                .header("Content-type", ContentType.JSON)
                .body(GSON.toJson(udto))
                .when()
                .put("/users/" + udto.getUserName())
                .then()
                .assertThat()
                .statusCode(200)
                .body("userName", equalTo("user"))
                .body("userEmail", equalTo("nytestemail@gmail.com"));
    }

    @Test
    void deleteUserTest() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("userName", udto.getUserName())
                .delete("/users/{userName}")
                .then()
                .statusCode(200);
    }
}