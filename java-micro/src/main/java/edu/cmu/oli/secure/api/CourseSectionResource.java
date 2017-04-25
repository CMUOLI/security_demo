package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.CourseSection;
import edu.cmu.oli.secure.logging.Logging;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@Path("sections")
public class CourseSectionResource {

    @Inject
    @Logging
    Logger log;

    @PersistenceContext
    EntityManager em;

    @Resource
    private ManagedExecutorService mes;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    public void all(@Suspended AsyncResponse response) {
        CompletableFuture.supplyAsync(() -> doAll(), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @GET
    @Path("{id}")
    public void findById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> doFindById(id), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @DELETE
    @Path("{id}")
    public void deleteById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> doDeleteById(id), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @POST
    public void create(@Suspended AsyncResponse response, JsonObject body) {
        CompletableFuture.supplyAsync(() -> doCreate(body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @PUT
    @Path("{id}")
    public void update(@Suspended AsyncResponse response,@PathParam("id") String id, JsonObject body) {
        CompletableFuture.supplyAsync(() -> doUpdate(id, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    public Response doAll() {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findAll", CourseSection.class);
        List<CourseSection> resultList = q.getResultList();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<CourseSection>>() {
        }.getType());

        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
//        Gson gson = new Gson();
//        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
//        je.addProperty("message", "Test Worked yeeaiii");
//        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doFindById(String id) {
        CourseSection singleResult = doFindByGuid(id);
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

    private CourseSection doFindByGuid(String id) {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findByGuid", CourseSection.class);
        q.setParameter("guid", id);
        return q.getSingleResult();
    }
    private CourseSection findByAdmitCode(String admitCode) {
        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findByAdmitCode", CourseSection.class);
        q.setParameter("admitCode", admitCode);
        return q.getSingleResult();
    }

    public Response doDeleteById(String id) {
        CourseSection singleResult = doFindByGuid(id);
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

    public Response doCreate(JsonObject body) {
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

    public Response doUpdate(String id, JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CourseSection courseSection = gson.fromJson(stWriter.toString(), new TypeToken<CourseSection>() {
        }.getType());
        CourseSection singleResult = doFindByGuid(id);
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
