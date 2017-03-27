package edu.cmu.oli.secure.api;

import edu.cmu.oli.secure.control.LearningActivityManager;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Raphael Gachuhi
 */
@Stateless
@Path("activity")
public class LearningActivityResource {

    @Inject
    Logger log;

    @Inject
    private LearningActivityManager learningActivityManager;

    @Resource
    private ManagedExecutorService mes;

    @GET
    @Path("all")
    public void all(@Suspended AsyncResponse response) {
        CompletableFuture.supplyAsync(() -> learningActivityManager.all(), mes).exceptionally(this::handelExceptions).thenAccept(response::resume);
    }

    @GET
    @Path("find/{id}")
    public void findById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> learningActivityManager.findById(id), mes).exceptionally(this::handelExceptions).thenAccept(response::resume);
    }

    @DELETE
    @Path("delete/{id}")
    public void deleteById(@Suspended AsyncResponse response, @PathParam("id") String id) {
        CompletableFuture.supplyAsync(() -> learningActivityManager.deleteById(id), mes).exceptionally(this::handelExceptions).thenAccept(response::resume);
    }

    @POST
    @Path("create")
    public void create(@Suspended AsyncResponse response, JsonObject body) {
        CompletableFuture.supplyAsync(() -> learningActivityManager.create(body), mes).exceptionally(this::handelExceptions).thenAccept(response::resume);
    }

    @PUT
    @Path("update/{id}")
    public void update(@Suspended AsyncResponse response,@PathParam("id") String id,
                        JsonObject body) {
        CompletableFuture.supplyAsync(() -> learningActivityManager.update(id, body), mes).exceptionally(this::handelExceptions).thenAccept(response::resume);
    }

    // Error handler
    private String handelExceptions(Throwable t) {
        String message = t.toString();
        log.log(Level.INFO, message);
        return message;
    }

}
