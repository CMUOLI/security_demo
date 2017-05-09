package edu.cmu.oli.secure;

import edu.cmu.oli.secure.api.CourseSectionResource;
import edu.cmu.oli.secure.api.QuestionResource;
import edu.cmu.oli.secure.api.RegistrationResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 *@author Raphael Gachuhi
 */
@ApplicationPath("api/v1")
public class JAXRSConfiguration extends Application {
//
//    @Override
//    public Set<Class<?>> getClasses() {
//        HashSet<Class<?>> set = new HashSet<Class<?>>();
//
//        set.add(CourseSectionResource.class);
//        set.add(QuestionResource.class);
//        set.add(RegistrationResource.class);
//        set.add(CorsFilter.class);
//
//        return set;
//    }
}
