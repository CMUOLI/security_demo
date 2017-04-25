package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.Question;
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
@Path("questions")
public class QuestionResource {

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
    public void update(@Suspended AsyncResponse response,@PathParam("id") String id,
                        JsonObject body) {
        CompletableFuture.supplyAsync(() -> doUpdate(id, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    public Response doAll() {
        TypedQuery<Question> q = em.createNamedQuery("Question.findAll", Question.class);
        List<Question> resultList = q.getResultList();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<Question>>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doFindById(String id) {
        Question singleResult = doFindByGuid(id);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Question not found " + id);
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        String json = gson.toJson(singleResult, new TypeToken<Question>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    private Question doFindByGuid(String id) {
        TypedQuery<Question> q = em.createNamedQuery("Question.findByGuid", Question.class);
        q.setParameter("guid", id);
        return q.getSingleResult();
    }
    private Question doFindByQuestionId(String questionId) {
        TypedQuery<Question> q = em.createNamedQuery("Question.findById", Question.class);
        q.setParameter("id", questionId);
        return q.getSingleResult();
    }

    public Response doDeleteById(String id) {
        Question singleResult = doFindByGuid(id);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Question not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        em.remove(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Question deleted");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doCreate(JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Question question = gson.fromJson(stWriter.toString(), new TypeToken<Question>() {
        }.getType());
        em.persist(question);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Question created");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doUpdate(String id, JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Question question = gson.fromJson(stWriter.toString(), new TypeToken<Question>() {
        }.getType());
        Question singleResult = doFindByGuid(id);
        if(singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "Question not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        singleResult.setTitle(question.getTitle());
        singleResult.setPrompt(question.getPrompt());
        singleResult.setPublished(question.isPublished());
        em.merge(singleResult);
        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "Question updated");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }
}
