package edu.cmu.oli.secure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *@author Raphael Gachuhi
 */
public class LoggerExposer {

    @Produces
    @Logging
    public Logger produceAuthnLog(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

}
