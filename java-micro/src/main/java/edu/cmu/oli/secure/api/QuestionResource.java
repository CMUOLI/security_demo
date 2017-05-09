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
@Path("{sectionId}/questions")
public class QuestionResource {

    @Inject
    @Logging
    Logger log;

    @Inject
    QuestionResourceManager qm;

    @Resource
    private ManagedExecutorService mes;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    public void findAllBySection(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> qm.doFindAllBySection(session, sectionId), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @GET
    @Path("{questionId}")
    public void findByIdInSection(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId, @PathParam("questionId") String questionId) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> qm.doFindByIdInSection(session, sectionId,questionId), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @DELETE
    @Path("{questionId}")
    public void deleteFromSection(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId, @PathParam("questionId") String questionId) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> qm.doDeleteFromSection(session, sectionId,questionId), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @POST
    public void createInSection(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId, JsonObject body) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> qm.doCreateInSection(session, sectionId, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @PUT
    @Path("{questionId}")
    public void updateFromSection(@Suspended AsyncResponse response, @PathParam("sectionId") String sectionId,@PathParam("questionId") String questionId,
                        JsonObject body) {
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        CompletableFuture.supplyAsync(() -> qm.doUpdateFromSection(session, sectionId, questionId, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

}
