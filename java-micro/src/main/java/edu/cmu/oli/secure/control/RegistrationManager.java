package edu.cmu.oli.secure.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.Registration;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@SecurityDomain("keycloak")
public class RegistrationManager {
    @PersistenceContext
    EntityManager em;

    @Inject
    Logger log;

    @PermitAll
    public Response all() {
        TypedQuery<Registration> q = em.createNamedQuery("Registration.findAll", Registration.class);
        List<Registration> resultList = q.getResultList();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<Registration>>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response findById(String id) {
        TypedQuery<Registration> q = em.createNamedQuery("Registration.findByGuid", Registration.class);
        q.setParameter("guid", id);
        Registration singleResult = q.getSingleResult();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Registration not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        String json = gson.toJson(singleResult, new TypeToken<Registration>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response deleteById(String id) {
        TypedQuery<Registration> q = em.createNamedQuery("Registration.findByGuid", Registration.class);
        q.setParameter("guid", id);
        Registration singleResult = q.getSingleResult();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Registration not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        em.remove(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Registration deleted");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response create(JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Registration registration = gson.fromJson(stWriter.toString(), new TypeToken<Registration>() {
        }.getType());
        em.persist(registration);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Registration created");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response update(String id, JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Registration registration = gson.fromJson(stWriter.toString(), new TypeToken<Registration>() {
        }.getType());
        TypedQuery<Registration> q = em.createNamedQuery("Registration.findByGuid", Registration.class);
        q.setParameter("guid", id);
        Registration singleResult = q.getSingleResult();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Registration not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        // Only role can be updated
        singleResult.setRole(registration.getRole());
        em.merge(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Registration updated");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }
}
