package edu.cmu.oli.secure.logging;

import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *@author Raphael Gachuhi
 */
public class LoggerExposer {

    @Produces
    public Logger expose(InjectionPoint ip) {
        String loggerName = ip.getMember().getDeclaringClass().getName();
        return Logger.getLogger(loggerName);
    }

}
