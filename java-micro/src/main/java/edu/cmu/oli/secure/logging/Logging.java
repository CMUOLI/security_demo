/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.oli.secure.logging;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * @version $Revision: 2.0 $
 * @author        Bill J Jerome
 * <a href="mailto:wjj@andrew.cmu.edu">(wjj@andrew.cmu.edu)</a>
 * @author        John A Rinderle
 * <a href="mailto:jar2@andrew.cmu.edu">(jar2@andrew.cmu.edu)</a>
 * @author Raphael Gachuhi
 * <a href="mailto:rgachuhi@andrew.cmu.edu">(rgachuhi@andrew.cmu.edu)</a>
 */
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface Logging {

}
