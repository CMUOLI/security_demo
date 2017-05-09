package edu.cmu.oli.secure.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.ResourceException;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.PoliciesResource;
import org.keycloak.admin.client.resource.PolicyResource;
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
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Raphael Gachuhi
 */
public class ApiUtil {
    public static final String MICRO_SERVICE = "java-micro";
    public static final String REALM = "security_zone";

    static Logger log = LoggerFactory.getLogger(ApiUtil.class);
    static AuthzClient authzClient;
    static AdapterConfig adapterConfig;
    static RealmResource realm;

    public static Set<String> authorize(KeycloakSecurityContext session, Set<String> roles, String sectionId, String filter, Set<String> scopes) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        String accessToken = session == null ? null : session.getTokenString();
        Set<String> realmRoles = session == null ? null : session.getToken().getRealmAccess().getRoles();
        if (accessToken == null || realmRoles == null) {
            String message = "Not Logged In";
            throw new ResourceException(Response.Status.FORBIDDEN, null, message);
        }
        if (Collections.disjoint(realmRoles, roles)) {
            String message = "Not Authorized";
            throw new ResourceException(Response.Status.FORBIDDEN, null, message);
        }
        // Assume Role based authorization (RBA) is desired if scopes or filters not supplied
        if (scopes == null || scopes.isEmpty() || filter == null) {
            return new HashSet<>();
        }
        // If requested section is not permitted
        Set<String> permittedPkgs = checkForPermissions(accessToken, filter, scopes);
        if (permittedPkgs == null) {
            String message = "Not authorized";
            throw new ResourceException(Response.Status.FORBIDDEN, null, message);
        }
        if (sectionId != null) {
            boolean permitted = false;
            for (String permittedPkg : permittedPkgs) {
                if (permittedPkg.contains(sectionId)) {
                    permitted = true;
                    break;
                }
            }
            if (!permitted) {
                String message = "Not authorized";
                throw new ResourceException(Response.Status.FORBIDDEN, null, message);
            }
        }
        return permittedPkgs;
    }

    /**
     * Catch all for errors thrown in service layers
     *
     * @param t
     * @return
     */
    static Response handelExceptions(Throwable t) {
        String message = t.getLocalizedMessage();
        log.info(message);
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        t = getRootThrowable(t);
        if (t instanceof ResourceException) {
            status = ((ResourceException) t).getStatus() == null ? Response.Status.NOT_FOUND : ((ResourceException) t).getStatus();
        }

        JsonObject je = new JsonObject();
        je.addProperty("messsage", message);
        return Response.status(status).entity(new Gson().toJson(je)).type(MediaType.APPLICATION_JSON).build();
    }

    private static Throwable getRootThrowable(Throwable t) {
        if (t.getCause() != null) {
            return getRootThrowable(t.getCause());
        }
        return t;
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

    public static Set<String> checkForPermissions(String accessToken, String filter, Set<String> scopes) {

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
            log.error("AuthorizationDeniedException ", ex);
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
            if (!Collections.disjoint(granted.getScopes(), scopes)) {
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
                    .serverUrl("http://128.237.220.60/auth")
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

    public static void createUserPolicy(String policyName, String userId) {
        // create Representation
        PolicyRepresentation userPolicyRepresentation = new PolicyRepresentation();
        userPolicyRepresentation.setName(policyName);
        userPolicyRepresentation.setDescription(policyName);
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

    public static void createOrUpdateUserJsPolicy(String userId, String sectionId, String... accessLevels) {
        PolicyResource policyResource = findPolicyResourceByName(userId);
        if (policyResource == null) {
            // create Representation
            PolicyRepresentation userPolicyRepresentation = new PolicyRepresentation();
            userPolicyRepresentation.setName(userId);
            userPolicyRepresentation.setDescription("Policy controlling user access to configured resources and scopes");
            userPolicyRepresentation.setLogic(Logic.POSITIVE);
            userPolicyRepresentation.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
            userPolicyRepresentation.setType("js");
            Map<String, String> config = new HashMap<>();
            String code = "var grantMap = {};\ngrantMap['" + sectionId + "'] = ['course_view_access', 'course_instruct_access'];\n\nvar permit = false;\nvar context = $evaluation.getContext();\nvar policy = $evaluation.getPolicy();\nvar userName = context.getIdentity().getAttributes().getValue(\"preferred_username\").asString(0);\nprint(\"User Name \" + userName);\n\nif (userName === policy.getName()) {\n    var resourcePermission = $evaluation.getPermission();\n    var resourceName = resourcePermission.getResource().getName();\n    print(\"Resource name \" + resourceName);\n    // Grant access only if resourceName and parentPolicy name match 'grantMap' above\n    if (grantMap.hasOwnProperty(resourceName)) {\n        var parentPolicy = $evaluation.getParentPolicy();\n        if (parentPolicy) {\n            print(\"Parent Policy name \" + parentPolicy.getName());\n            grantMap[resourceName].forEach(function (s) {\n                if (s === parentPolicy.getName()) {\n                    permit = true;\n                }\n            });\n        }\n    }\n\n}\n\nif (permit) {\n    $evaluation.grant();\n}";
            config.put("code", code);
            userPolicyRepresentation.setConfig(config);
            final ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
            // create
            Response response = getRealm().clients().get(clientRepresentation.getId()).authorization().policies().create(userPolicyRepresentation);
            response.close();
            return;
        }
        PolicyRepresentation policyRepresentation = policyResource.toRepresentation();
        if(!policyRepresentation.getType().equalsIgnoreCase("js")){
            String message = "Wrong type for a user js policy " + policyRepresentation.getType();
            log.error(message);
            throw new ResourceException(Response.Status.INTERNAL_SERVER_ERROR, null, message);
        }
        Map<String, String> config = policyRepresentation.getConfig();
        String code = config.get("code");
        code = code.substring(code.indexOf(";")+1);
        String allCode = "var grantMap = {};\ngrantMap['" + sectionId + "'] = ['course_view_access', 'course_instruct_access'];" + code;
        config.put("code", allCode);
        policyResource.update(policyRepresentation);
    }

    public static void updateUserPolicy(String policyName, String userId, boolean addUser) {
        PolicyResource policyResource = findPolicyResourceByName(policyName);
        if (policyResource == null) {
            return;
        }
        PolicyRepresentation policyRepresentation = policyResource.toRepresentation();
        Map<String, String> config = policyRepresentation.getConfig();
        String users = config.get("users");
        log.info("updateUserPolicy users JSON " + users);
        if (users != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<Set<String>>() {
            }.getType();
            Set<String> ids = gson.fromJson(users, listType);
            if (addUser) {
                ids.add(userId);
            } else {
                ids.remove(userId);
            }
            String json = gson.toJson(ids, listType);
            config.put("users", json);
            policyResource.update(policyRepresentation);
        }
    }

    public static void deletePolicy(String policyName) {
        PolicyResource policyResource = findPolicyResourceByName(policyName);
        if (policyResource == null) {
            return;
        }
        policyResource.remove();
    }

    public static void createRoleScopePolicy(String policyName, String policy, String resource, String scope) {
        // create Representation
        PolicyRepresentation userPolicyRepresentation = new PolicyRepresentation();
        userPolicyRepresentation.setName(policyName);
        userPolicyRepresentation.setDescription(policyName);
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

    private static PolicyResource findPolicyResourceByName(String policyName) {
        ClientRepresentation clientRepresentation = getRealm().clients().findByClientId(MICRO_SERVICE).get(0);
        PoliciesResource policies = getRealm().clients().get(clientRepresentation.getId()).authorization().policies();
        PolicyRepresentation policyRep = policies.policies()
                .stream().filter(policyRepresentation -> policyRepresentation.getName().equals(policyName))
                .findFirst().orElse(null);
        if (policyRep != null) {
            return policies.policy(policyRep.getId());
        }
        return null;
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