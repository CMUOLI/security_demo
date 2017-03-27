package edu.cmu.oli.secure.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.cmu.oli.secure.domain.*;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
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
