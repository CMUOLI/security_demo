FROM jboss/wildfly:latest
MAINTAINER Raphael Gachuhi, oli.cmu.edu
ENV DEPLOYMENT_DIR ${JBOSS_HOME}/standalone/deployments/
ADD execute.sh ${JBOSS_HOME}/
USER root
RUN chmod +x ${JBOSS_HOME}/execute.sh
USER jboss
RUN cd $JBOSS_HOME \
    && curl -O https://downloads.jboss.org/keycloak/3.0.0.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && tar xf keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && rm keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/bin/adapter-install-offline.cli \
    && cd $HOME
COPY target/secure.war $DEPLOYMENT_DIR
ENTRYPOINT ${JBOSS_HOME}/execute.sh
