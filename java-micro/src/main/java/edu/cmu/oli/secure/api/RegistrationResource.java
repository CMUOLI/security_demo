package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.Registration;
import edu.cmu.oli.secure.logging.Logging;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@Path("registrations")
public class RegistrationResource {

    @Inject
    @Logging
    Logger log;

    @Inject
    RegistrationResourceManager rm;

    @Resource
    private ManagedExecutorService mes;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    public void all(@Suspended AsyncResponse response) {
        CompletableFuture.supplyAsync(() -> rm.doAll(), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @GET
    @Path("{id}")
    public void findById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> rm.doFindById(id), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @DELETE
    @Path("{id}")
    public void deleteById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> rm.doDeleteById(id), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @POST
    public void create(@Suspended AsyncResponse response, JsonObject body) {
        CompletableFuture.supplyAsync(() -> rm.doCreate(body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

    @PUT
    @Path("{id}")
    public void update(@Suspended AsyncResponse response,@PathParam("id") String id, JsonObject body) {
        CompletableFuture.supplyAsync(() -> rm.doUpdate(id, body), mes).exceptionally(ApiUtil::handelExceptions).thenAccept(response::resume);
    }

}
