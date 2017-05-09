package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.ResourceException;
import edu.cmu.oli.secure.domain.Question;
import edu.cmu.oli.secure.logging.Logging;
import org.keycloak.KeycloakSecurityContext;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static edu.cmu.oli.secure.api.ApiUtil.authorize;
import static edu.cmu.oli.secure.api.Roles.*;

/**
 * @author Raphael Gachuhi
 */
@Stateless
public class QuestionResourceManager {

    @Inject
    @Logging
    Logger log;

    @PersistenceContext
    EntityManager em;

    public Response doFindAllBySection(KeycloakSecurityContext session, String sectionId) {
        if(sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Allow only users with permissions for this section to fetch it
        authorize(session, new HashSet(Arrays.asList(USER_ROLE)),
                sectionId, "name="+sectionId, new HashSet<>(Arrays.asList(Scopes.VIEW_MATERIAL_ACTION)));

        TypedQuery<Question> q = em.createNamedQuery("Question.findAll", Question.class);
        List<Question> resultList = q.getResultList();
        resultList = resultList.stream().filter(qs ->qs.getCourseSection().getGuid().equalsIgnoreCase(sectionId)).collect(Collectors.toList());
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<Question>>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doFindByIdInSection(KeycloakSecurityContext session, String sectionId, String id) {
        if(id == null || sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Limit access to those with sufficient permissions for section
        authorize(session, new HashSet(Arrays.asList(USER_ROLE)),
                sectionId, "name="+sectionId, new HashSet<>(Arrays.asList(Scopes.VIEW_MATERIAL_ACTION)));
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

    public Response doDeleteFromSection(KeycloakSecurityContext session, String sectionId, String id) {
        if(id == null || sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Limit access to those with sufficient permissions for section
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)),
                sectionId, "name=" + sectionId, new HashSet(Arrays.asList(Scopes.INSTRUCT_MATERIAL_ACTION)));
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

    public Response doCreateInSection(KeycloakSecurityContext session, String sectionId, JsonObject body) {
        if(sectionId == null || body == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Limit access to those with sufficient permissions for section
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)),
                sectionId, "name=" + sectionId, new HashSet(Arrays.asList(Scopes.INSTRUCT_MATERIAL_ACTION)));
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

    public Response doUpdateFromSection(KeycloakSecurityContext session, String sectionId, String id, JsonObject body) {
        if(id == null || body == null || sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Limit access to those with sufficient permissions for section
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)),
                sectionId, "name=" + sectionId, new HashSet(Arrays.asList(Scopes.INSTRUCT_MATERIAL_ACTION)));

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
}
