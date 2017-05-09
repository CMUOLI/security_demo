FROM jboss/wildfly:latest
MAINTAINER Raphael Gachuhi, oli.cmu.edu
RUN cd $JBOSS_HOME \
    && curl -O https://downloads.jboss.org/keycloak/3.0.0.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && tar xf keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && rm keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
    && ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/bin/adapter-install-offline.cli \
    && cd $HOME
ENV DEPLOYMENT_DIR ${JBOSS_HOME}/standalone/deployments/
USER root
#RUN yum install zip unzip -y
ADD execute.sh ${JBOSS_HOME}/
ADD wait-for-it.sh ${JBOSS_HOME}/
RUN chmod +x ${JBOSS_HOME}/execute.sh && chmod +x ${JBOSS_HOME}/wait-for-it.sh
USER jboss
#ADD resteasy-jboss-modules-3.1.2.Final.zip $JBOSS_HOME/modules/system/layers/base/
#RUN cd $JBOSS_HOME/modules/system/layers/base/ \
#    && unzip resteasy-jboss-modules-3.1.2.Final.zip
#COPY target/authorization.war $DEPLOYMENT_DIR
ENTRYPOINT ${JBOSS_HOME}/execute.sh
#ENV DEPLOYMENT_DIR ${JBOSS_HOME}/target

#ADD execute.sh ${JBOSS_HOME}/
#RUN mkdir -p ${JBOSS_HOME}/java-micro
#COPY . ${JBOSS_HOME}/java-micro
#USER root
#RUN chmod +x ${JBOSS_HOME}/java-micro/execute.sh ${JBOSS_HOME}/java-micro/wait-for-it.sh
#USER jboss
#RUN cd $JBOSS_HOME \
#    && curl -O https://downloads.jboss.org/keycloak/3.0.0.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
#    && tar xf keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
#    && rm keycloak-wildfly-adapter-dist-3.0.0.Final.tar.gz \
#    && ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/bin/adapter-install-offline.cli
#RUN curl -O http://mirror.olnevhost.net/pub/apache/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
#RUN tar xvf apache-maven-3.0.5-bin.tar.gz
#RUN mv apache-maven-3.0.5 ${JBOSS_HOME}/apache-maven
#ENV M2_HOME ${JBOSS_HOME}/apache-maven
#ENV M2 ${M2_HOME}/bin
#ENV PATH ${M2}:${PATH}
#RUN mvn clean package -f ${JBOSS_HOME}/java-micro/pom.xml
#ENTRYPOINT ${JBOSS_HOME}/java-micro/execute.sh
