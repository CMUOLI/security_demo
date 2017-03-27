package edu.cmu.oli.secure.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.*;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@SecurityDomain("keycloak")
public class QuestionManager {
    @PersistenceContext
    EntityManager em;

    @Inject
    Logger log;

    public Response all() {
        TypedQuery<Question> q = em.createNamedQuery("Question.findAll", Question.class);
        List<Question> resultList = q.getResultList();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<Question>>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response findById(String id) {
        Question singleResult = findByGuid(id);
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

    private Question findByGuid(String id) {
        TypedQuery<Question> q = em.createNamedQuery("Question.findByGuid", Question.class);
        q.setParameter("guid", id);
        return q.getSingleResult();
    }
    private Question findByQuestionId(String questionId) {
        TypedQuery<Question> q = em.createNamedQuery("Question.findById", Question.class);
        q.setParameter("id", questionId);
        return q.getSingleResult();
    }

    public Response deleteById(String id) {
        Question singleResult = findByGuid(id);
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

    public Response create(JsonObject body) {
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

    public Response update(String id, JsonObject body) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(body);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Question question = gson.fromJson(stWriter.toString(), new TypeToken<Question>() {
        }.getType());
        Question singleResult = findByGuid(id);
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
