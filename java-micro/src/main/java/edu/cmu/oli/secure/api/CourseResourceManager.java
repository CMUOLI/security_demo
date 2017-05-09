package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.ResourceException;
import edu.cmu.oli.secure.domain.CourseSection;
import edu.cmu.oli.secure.logging.Logging;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static edu.cmu.oli.secure.api.ApiUtil.authorize;
import static edu.cmu.oli.secure.api.Roles.*;

/**
 * @author Raphael Gachuhi
 */
@Stateless
public class CourseResourceManager {

    @Inject
    @Logging
    Logger log;

    @PersistenceContext
    EntityManager em;

    public Response doAll(KeycloakSecurityContext session) {

        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findAll", CourseSection.class);
        List<CourseSection> resultList = q.getResultList();
        if (!resultList.isEmpty()) {
            // Filter to authorized sections only
            Set<String> authorized = authorize(session, new HashSet(Arrays.asList(USER_ROLE)),
                    null, "type="+CourseSectionResource.resourceType,
                    new HashSet<>(Arrays.asList(Scopes.VIEW_MATERIAL_ACTION, Scopes.INSTRUCT_MATERIAL_ACTION)));
            if (authorized == null) {
                String message = "Not authorized";
                throw new ResourceException(Response.Status.FORBIDDEN, null, message);
            }
            resultList = resultList.stream().filter(cp -> authorized.contains(cp.getGuid())).collect(Collectors.toList());
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(resultList, new TypeToken<ArrayList<CourseSection>>() {
        }.getType());

        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doFindById(KeycloakSecurityContext session, String sectionId) {
        if(sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
         //Allow only users with permissions for this section to fetch it
        authorize(session, new HashSet(Arrays.asList(USER_ROLE)),
                sectionId, "name="+sectionId, new HashSet<>(Arrays.asList(Scopes.VIEW_MATERIAL_ACTION, Scopes.INSTRUCT_MATERIAL_ACTION)));

        CourseSection singleResult = doFindByGuid(sectionId);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if (singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found " + sectionId);
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        String json = gson.toJson(singleResult, new TypeToken<CourseSection>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doDeleteById(KeycloakSecurityContext session, String sectionId) {
        if(sectionId == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Allow findBySection admins and any instructor with permissions for this section to delete it
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)),
                sectionId, "name=" + sectionId, new HashSet(Arrays.asList(Scopes.INSTRUCT_MATERIAL_ACTION)));

        CourseSection singleResult = doFindByGuid(sectionId);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        if (singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        em.remove(singleResult);

        com.google.gson.JsonObject je = new com.google.gson.JsonObject();
        je.addProperty("messsage", "CourseSection deleted");
        return Response.status(Response.Status.OK).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public Response doCreate(KeycloakSecurityContext session, JsonObject body) {
        if(body == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)), null, null, null);
        JsonValue course = body.get("course");
        if(course == null){
            String message = "Valication exception";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject((JsonObject)course);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        CourseSection courseSection = gson.fromJson(stWriter.toString(), new TypeToken<CourseSection>() {
        }.getType());
        em.persist(courseSection);
        em.flush();

        log.info("New Section " + courseSection.toString());
        createKeycloakResource(session, courseSection);

        String json = gson.toJson(courseSection, new TypeToken<CourseSection>() {
        }.getType());
        return Response.status(Response.Status.OK).entity(json).type(MediaType.APPLICATION_JSON).build();
    }

    private void createKeycloakResource(KeycloakSecurityContext session, CourseSection courseSection) {
//        TypedQuery<CourseSection> q = em.createNamedQuery("CourseSection.findByAdmitCode", CourseSection.class);
//        q.setParameter("admitCode", admitCode);
//        CourseSection courseSection =  q.getSingleResult();

        ApiUtil.createResource(courseSection.getGuid(), "/courses/" + courseSection.getGuid(),
                CourseSectionResource.resourceType,
                Arrays.asList(Scopes.VIEW_MATERIAL_ACTION, Scopes.INSTRUCT_MATERIAL_ACTION));
        ApiUtil.createOrUpdateUserJsPolicy(session.getToken().getPreferredUsername(), courseSection.getGuid());
        //return courseSection;
    }

    public Response doUpdate(KeycloakSecurityContext session, String sectionId, JsonObject body) {
        if(sectionId == null || body == null){
            String message = "Parameters missing";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        // Allow findBySection admins and any instructor with right permissions for this section to update it
        authorize(session, new HashSet(Arrays.asList(ADMIN_ROLE, INSTRUCTOR_ROLE)),
                sectionId, "name=" + sectionId, new HashSet(Arrays.asList(Scopes.INSTRUCT_MATERIAL_ACTION)));

        JsonValue course = body.get("course");
        if(course == null){
            String message = "Valication exception";
            throw new ResourceException(Response.Status.BAD_REQUEST, null, message);
        }
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject((JsonObject)course);
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        CourseSection courseSection = gson.fromJson(stWriter.toString(), new TypeToken<CourseSection>() {
        }.getType());
        CourseSection singleResult = doFindByGuid(sectionId);
        if (singleResult == null) {
            com.google.gson.JsonObject je = new com.google.gson.JsonObject();
            je.addProperty("messsage", "CourseSection not found");
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(je)).type(MediaType.APPLICATION_JSON).build();
        }
        singleResult.setTitle(courseSection.getTitle());
        singleResult.setAdmitCode(courseSection.getAdmitCode());
        em.merge(singleResult);

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
}
