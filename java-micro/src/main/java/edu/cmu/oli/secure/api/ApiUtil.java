package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.cmu.oli.secure.ResourceException;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.*;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBAccessException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * @author Raphael Gachuhi
 */
public class ApiUtil {
    public static final String MICRO_SERVICE = "java-micro";
    public static final String REALM = "security_demo";
    public static final String ADMIN_ROLE = "admin";
    public static final String INSTRUCTOR = "instructor";
    public static final String STUDENT = "student";


    private static Logger log = LoggerFactory.getLogger(ApiUtil.class);
    private static AuthzClient authzClient;
    private static AdapterConfig adapterConfig;
    private static RealmResource realm;

    /**
     * Catch all for errors thrown in service layers
     *
     * @param t
     * @return
     */
    static Response handelExceptions(Throwable t) {
        String message = t.toString();
        log.info(message);

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (t instanceof ResourceException) {
            Response.Status errstatus = ((ResourceException) t).getStatus();
            status = errstatus == null ? Response.Status.NOT_FOUND : errstatus;
        } else if (t instanceof EJBAccessException || message.toLowerCase().contains("not allowed")) {
            status = Response.Status.FORBIDDEN;
            message = "Request Forbidden";
        }
        JsonObject je = new JsonObject();
        je.addProperty("messsage", message);
        return Response.status(status).entity(new Gson().toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    public static AuthzClient getAuthzClient() {
        if (authzClient == null) {
            try {
                adapterConfig = JsonSerialization.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream("keycloak.json"), AdapterConfig.class);
                Configuration configuration = new Configuration(adapterConfig.getAuthServerUrl(), adapterConfig.getRealm(), adapterConfig.getResource(), adapterConfig.getCredentials(), null);
                authzClient = AuthzClient.create(configuration);
            } catch (Exception e) {
                throw new RuntimeException("Could not create authorization client.", e);
            }
        }
        return authzClient;
    }

    public static void deleteResource(String name) {
        AuthzClient authzClient = getAuthzClient();
        ProtectedResource resourceClient = authzClient.protection().resource();
        Set<String> existingResource = resourceClient.findByFilter("name=" + name);

        if (!existingResource.isEmpty()) {
            resourceClient.delete(existingResource.iterator().next());
        }
    }

    public static void createResource(String name, String uri, String type, List<String> scopes) {
        AuthzClient authzClient = getAuthzClient();
        // create a new resource representation with the information we want
        ResourceRepresentation newResource = new ResourceRepresentation();

        newResource.setName(name);
        newResource.setType(type);
        newResource.setUri(uri);

        for (String scope : scopes) {
            newResource.addScope(new ScopeRepresentation(scope));
        }

        ProtectedResource resourceClient = authzClient.protection().resource();
        Set<String> existingResource = resourceClient.findByFilter("name=" + newResource.getName());

        if (!existingResource.isEmpty()) {
            resourceClient.delete(existingResource.iterator().next());
        }

        // create the resource on the server
        RegistrationResponse response = resourceClient.create(newResource);
        String resourceId = response.getId();

        // query the resource using its newly generated id
        ResourceRepresentation resource = resourceClient.findById(resourceId).getResourceDescription();

        log.info(resource.toString());
    }

    public static void updateResource(String name, String uri, String type, List<String> scopes) {
        AuthzClient authzClient = getAuthzClient();
        // create a new resource representation with the information we want
        ResourceRepresentation resource = new ResourceRepresentation();

        resource.setName(name);
        for (String scope : scopes) {
            resource.addScope(new ScopeRepresentation(scope));
        }

        ProtectedResource resourceClient = authzClient.protection().resource();
        Set<String> existingResource = resourceClient.findByFilter("name=" + resource.getName());

        if (existingResource.isEmpty()) {
            createResource(name, uri, type, scopes);
        }

        resource.setId(existingResource.iterator().next());
        resource.setUri(uri);

        // update the resource on the server
        resourceClient.update(resource);

        // query the resource using its newly generated id
        ResourceRepresentation existing = resourceClient.findById(resource.getId()).getResourceDescription();

        log.info(existing.toString());
    }

    public static Set<String> introspectRequestingPartyToken(String accessToken, String filter, String scope) {

        AuthzClient authzClient = ApiUtil.getAuthzClient();

        // query the server for a resource with a given name
        Set<String> resourceIds = authzClient.protection()
                .resource()
                .findByFilter(filter);

        // create an entitlement request
        EntitlementRequest request = new EntitlementRequest();
        for (String resourceId : resourceIds) {
            PermissionRequest permission = new PermissionRequest();
            permission.setResourceSetId(resourceId);
            request.addPermission(permission);
        }

        // send the entitlement request to the server in order to
        // obtain an RPT with all permissions granted to the user
        EntitlementResponse response = null;
        try {
            response = authzClient.entitlement(accessToken).get(MICRO_SERVICE, request);
        } catch (Exception ex) {
            log.debug("AuthorizationDeniedException ", ex);
            return null;
        }
        String rpt = response.getRpt();

        log.info("Requesting Party Token: " + rpt);

        TokenIntrospectionResponse requestingPartyToken = authzClient.protection().introspectRequestingPartyToken(rpt);

        log.info("Token status is: " + requestingPartyToken.getActive());
        log.info("Permissions granted by the server: ");
        Map<String, Object> otherClaims = requestingPartyToken.getOtherClaims();
        Set<Map.Entry<String, Object>> entries = otherClaims.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            log.info(entry.getKey() + "---" + entry.getValue());
        }
        Set<String> resourceNames = new HashSet<>();
        List<Permission> permissions = requestingPartyToken.getPermissions();
        for (Permission granted : permissions) {
            log.info(granted.toString());
            if (granted.getScopes().contains(scope)) {
                resourceNames.add(granted.getResourceSetName());
                log.info(granted.getResourceSetName());
            }
        }
        return resourceNames;
    }

    private static List<Permission> obtainEntitlementsForResource(String resourceName, String accessToken) {
        AuthzClient authzClient = getAuthzClient();

        // create an entitlement request
        EntitlementRequest request = new EntitlementRequest();
        PermissionRequest permission = new PermissionRequest();
        permission.setResourceSetName(resourceName);

        request.addPermission(permission);

        // send the entitlement request to the server in order to obtain a RPT
        // with all permissions granted to the user
        EntitlementResponse response;
        try {

            response = authzClient.entitlement(accessToken).get(MICRO_SERVICE, request);
        } catch (Exception ex) {
            log.debug("AuthorizationDeniedException ", ex);
            return null;
        }
        String rpt = response.getRpt();

        log.info("obtainEntitlementsForResource RPT: " + rpt);
        TokenIntrospectionResponse requestingPartyToken = authzClient.protection().introspectRequestingPartyToken(rpt);

        return requestingPartyToken.getPermissions();
    }

    private static List<Permission> obtainAllEntitlements(String accessToken) {
        AuthzClient authzClient = getAuthzClient();

        // send the entitlement request to the server in order to obtain a RPT with all permissions granted to the user
        EntitlementResponse response;
        try {
            response = authzClient.entitlement(accessToken).getAll(MICRO_SERVICE);
        } catch (Exception ex) {
            log.debug("AuthorizationDeniedException ", ex);
            return null;
        }
        String rpt = response.getRpt();

        log.info("You got a RPT: " + rpt);
        TokenIntrospectionResponse requestingPartyToken = authzClient.protection().introspectRequestingPartyToken(rpt);

        return requestingPartyToken.getPermissions();
    }

    /**
     * Obtain an Entitlement API Token or EAT from the server. Usually, EATs are going to be obtained by clients using a
     * authorization_code grant type. Here we are using resource owner credentials for demonstration purposes.
     * Good for use in inter-service communication
     *
     * @return a string representing an EAT
     */
    private static String getAccessToken() {
        return getAuthzClient().obtainAccessToken(MICRO_SERVICE, adapterConfig.getCredentials().get("secret").toString()).getToken();
    }

    static RealmResource getRealm() {
        if (realm == null) {
            Keycloak kc = KeycloakBuilder.builder()
                    .serverUrl("http://128.237.215.67/auth")
                    .realm("master")
                    .username("admin")
                    .password("admin")
                    .clientId("admin-cli")
                    .resteasyClient(
                            new ResteasyClientBuilder()
                                    .connectionPoolSize(10).build()
                    ).build();
            realm = kc.realm(REALM);
        }
        return realm;
    }

    public static void createUserPolicy(String name, String userId) {
        // create Representation
        PolicyRepresentation userPolicyRepresentation = new PolicyRepresentation();
        userPolicyRepresentation.setName(name);
        userPolicyRepresentation.setDescription(name);
        userPolicyRepresentation.setLogic(Logic.POSITIVE);
        userPolicyRepresentation.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        userPolicyRepresentation.setType("user");
        Map<String, String> config = new HashMap<>();

        List<String> ids = Arrays.asList(userId);
        String s = null;
        try {
            s = JsonSerialization.writeValueAsString(ids);
        } catch (IOException e) {
        }
        config.put("users", s);
        userPolicyRepresentation.setConfig(config);
        final ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        // create
        Response response = getRealm().clients().get(clientRepresentation.getId()).authorization().policies().create(userPolicyRepresentation);
        response.close();
    }

    public static void deletePolicy(String name) {
//        AuthzClient authzClient = getAuthzClient();
//        ProtectedResource resourceClient = authzClient.protection().resource();
//        Set<String> existingResource = resourceClient.findByFilter("name=" + name);
//
//        if (!existingResource.isEmpty()) {
//            resourceClient.delete(existingResource.iterator().next());
//        }
    }

    public static void createRoleScopePolicy(String name, String policy, String resource, String scope) {
        // create Representation
        PolicyRepresentation userPolicyRepresentation = new PolicyRepresentation();
        userPolicyRepresentation.setName(name);
        userPolicyRepresentation.setDescription(name);
        userPolicyRepresentation.setLogic(Logic.POSITIVE);
        userPolicyRepresentation.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        userPolicyRepresentation.setType("scope");
        Map<String, String> config = new HashMap<>();

        if (policy != null) {
            config.put("applyPolicies", buildConfigOption(findPolicyByName(policy).getId()));
        }

        config.put("resources", buildConfigOption(findResourceByName(resource).getId()));

        config.put("scopes", buildConfigOption(findScopeByName(scope).getId()));

        userPolicyRepresentation.setConfig(config);
        final ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        // create
        Response response = getRealm().clients().get(clientRepresentation.getId()).authorization().policies().create(userPolicyRepresentation);
        response.close();
    }

    private static String buildConfigOption(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append("\"" + value + "\"");
        }

        return builder.insert(0, "[").append("]").toString();
    }

    private static PolicyRepresentation findPolicyByName(String name) {
        ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        return getRealm().clients().get(clientRepresentation.getId()).authorization().policies().policies()
                .stream().filter(policyRepresentation -> policyRepresentation.getName().equals(name))
                .findFirst().orElse(null);
    }

    private static org.keycloak.representations.idm.authorization.ResourceRepresentation findResourceByName(String name) {
        ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        return getRealm().clients().get(clientRepresentation.getId()).authorization().resources().resources()
                .stream().filter(resource -> resource.getName().equals(name))
                .findFirst().orElse(null);
    }

    private static org.keycloak.representations.idm.authorization.ScopeRepresentation findScopeByName(String name) {
        ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        return getRealm().clients().get(clientRepresentation.getId()).authorization().scopes().scopes()
                .stream().filter(scope -> scope.getName().equals(name))
                .findFirst().orElse(null);
    }
}
