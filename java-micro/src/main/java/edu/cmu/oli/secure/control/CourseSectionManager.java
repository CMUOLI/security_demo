package edu.cmu.oli.secure.control;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.CourseSection;
import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * @author Raphael Gachuhi
 */
@Stateless

@SecurityDomain("keycloak")
public class CourseSectionManager {
    @PersistenceContext
    EntityManager em;

    @Inject
    Logger log;

    public Response all() {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findAll", CourseSection.class);
        List<CourseSection> resultList = q.getResultList();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<CourseSection>>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response findById(String id) {
        CourseSection singleResult = findByGuid(id);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        String json = gson.toJson(singleResult, new TypeToken<CourseSection>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    private CourseSection findByGuid(String id) {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findByGuid", CourseSection.class);
        q.setParameter("guid", id);
        return q.getSingleResult();
    }
    private CourseSection findByAdmitCode(String admitCode) {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findByAdmitCode", CourseSection.class);
        q.setParameter("admitCode", admitCode);
        return q.getSingleResult();
    }

    public Response deleteById(String id) {
        CourseSection singleResult = findByGuid(id);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        em.remove(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "CourseSection deleted");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response create(JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CourseSection courseSection = gson.fromJson(stWriter.toString(), new TypeToken<CourseSection>() {
        }.getType());
        em.persist(courseSection);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "CourseSection created");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response update(String id, JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CourseSection courseSection = gson.fromJson(stWriter.toString(), new TypeToken<CourseSection>() {
        }.getType());
        CourseSection singleResult = findByGuid(id);
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        singleResult.setTitle(courseSection.getTitle());
        singleResult.setAdmitCode(courseSection.getAdmitCode());
        em.merge(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "CourseSection updated");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }
}
