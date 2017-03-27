package edu.cmu.oli.secure.control;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;
import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * @author Raphael Gachuhi
 */
@Stateless

@SecurityDomain("keycloak")
public class CourseSectionManager {
    @PersistenceContext
    EntityManager em;

    @Inject
    Logger log;

    @RolesAllowed("admin")
    public String all(){

        return " ";
    }

    public String findById(String id){
        return " ";
    }

    public String deleteById(String id){
        return " ";
    }

    public String create(JsonObject body){
        return " ";
    }

    public String update(String id, JsonObject body){
        return " ";
    }

//
//    private String find(String query, Type type, String id, boolean delete) {
//        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//        Query q = em.createNamedQuery(query);
//        if (id == null) {
//            List results = q.getResultList();
//            return gson.toJson(results, type);
//        } else {
//            q.setParameter("uniqueId", id);
//            Object singleResult = q.getSingleResult();
//            if (singleResult != null && delete) {
//                em.remove(singleResult);
//                return "deleted";
//            }
//            List results = new ArrayList();
//            results.add(singleResult);
//            return gson.toJson(results, type);
//        }
//    }
//
//    private Object findId(String query, String id) {
//        Query q = em.createNamedQuery(query);
//        q.setParameter("uniqueId", id);
//        return q.getSingleResult();
//    }
}
