package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import datafacades.UserFacade;
import dtofacades.UserDTOFacade;
import dtos.UserDTO;
import errorhandling.API_Exception;
import utils.EMF_Creator;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

@Path("users")
public class UserResource {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private UserFacade userFacade = UserFacade.getUserFacade(EMF);
    private UserDTOFacade dtoFacade = UserDTOFacade.getInstance(EMF);
    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();


    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllUsers() throws API_Exception {
        return Response.ok().entity(GSON.toJson(dtoFacade.getAllUsers())).type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }

    @GET
    @Path("/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserByUserName(@PathParam("userName") String userName) throws API_Exception {
        return Response.ok().entity(GSON.toJson(dtoFacade.getUserByUserName(userName))).type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createUser(String content) throws API_Exception {
        UserDTO userDTO = GSON.fromJson(content, UserDTO.class);
        UserDTO newUserDTO = dtoFacade.createUser(userDTO);
        return Response.ok().entity(GSON.toJson(newUserDTO)).type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }

    @PUT
    @Path("/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateUser(@PathParam("userName")String userName, String content) throws EntityNotFoundException, API_Exception {
        UserDTO udto = GSON.fromJson(content, UserDTO.class);
        udto.setUserName(userName);
        UserDTO updatedUser = dtoFacade.updateUser(udto);
        return Response.ok().entity(GSON.toJson(updatedUser)).type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }

    @DELETE
    @Path("/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteUser(@PathParam("userName") String userName) throws API_Exception {
        UserDTO deletedUser = dtoFacade.deleteUser(userName);
        return Response.ok().entity(GSON.toJson(deletedUser)).type(MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name())).build();
    }
}