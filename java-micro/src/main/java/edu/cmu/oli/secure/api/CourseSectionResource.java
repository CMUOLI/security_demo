package edu.cmu.oli.secure.api;

import edu.cmu.oli.secure.logging.Logging;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import java.util.concurrent.CompletableFuture;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@Path("courses")
public class CourseSectionResource {

    public static String resourceType="urn:java-micro:courses:course_section";

    @Inject
    @Logging
    Logger log;

    @Inject
    CourseResourceManager cm;

    @Resource
    private ManagedExecutorService mes;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    public void all(@Suspended AsyncResponse response) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> cm.doAll(session), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @GET
    @Path("{sectionId}")
    public void findById(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> cm.doFindById(session,sectionId), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @DELETE
    @Path("{sectionId}")
    public void deleteById(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> cm.doDeleteById(session,sectionId), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @POST
    public void create(@Suspended AsyncResponse response, JsonObject body) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> cm.doCreate(session,body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @PUT
    @Path("{sectionId}")
    public void update(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId, JsonObject body) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> cm.doUpdate(session,sectionId, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }
}
