package edu.cmu.oli.secure.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.Registration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

        return null;
    }

    public Response update(String id, JsonObject body) {

        return null;
    }

//
//    private String find(String query, Type type, String id, boolean delete) {
//        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//        Query q = em.createNamedQuery(query);
//        if (id == null) {
//            List results = q.getResultList();
//            return gson.toJson(results, type);
//        } else {
//            q.setParameter("uniqueId", id);
//            Object singleResult = q.getSingleResult();
//            if (singleResult != null && delete) {
//                em.remove(singleResult);
//                return "deleted";
//            }
//            List results = new ArrayList();
//            results.add(singleResult);
//            return gson.toJson(results, type);
//        }
//    }
//
//    private Object findId(String query, String id) {
//        Query q = em.createNamedQuery(query);
//        q.setParameter("uniqueId", id);
//        return q.getSingleResult();
//    }
}
