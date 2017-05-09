FROM jboss/base-jdk:8

ENV KEYCLOAK_VERSION 3.1.0.Final
# Enables signals getting passed from startup script to JVM
# ensuring clean shutdown when container is stopped.
ENV LAUNCH_JBOSS_IN_BACKGROUND 1
ENV PROXY_ADDRESS_FORWARDING false
USER root

RUN yum install -y epel-release && yum install -y jq && yum clean all

ADD keycloak-3.1.0.Final.tar.gz /opt/jboss/

RUN mv /opt/jboss/keycloak-3.1.0.Final /opt/jboss/keycloak \
   && chown -R jboss.jboss /opt/jboss/keycloak \
   && ls -alFh /opt/jboss/

USER jboss

ADD docker-entrypoint.sh /opt/jboss/

ADD setLogLevel.xsl /opt/jboss/keycloak/
RUN java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone.xml -xsl:/opt/jboss/keycloak/setLogLevel.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone.xml

ENV JBOSS_HOME /opt/jboss/keycloak

#Enabling Proxy address forwarding so we can correctly handle SSL termination in front ends
#such as an OpenShift Router or Apache Proxy
RUN sed -i -e 's/<http-listener /& proxy-address-forwarding="${env.PROXY_ADDRESS_FORWARDING}" /' $JBOSS_HOME/standalone/configuration/standalone.xml

EXPOSE 8080

ADD changeDatabase.xsl /opt/jboss/keycloak/
RUN java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone.xml -xsl:/opt/jboss/keycloak/changeDatabase.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone.xml; java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml -xsl:/opt/jboss/keycloak/changeDatabase.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml; rm /opt/jboss/keycloak/changeDatabase.xsl
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main; cd /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main && curl -O http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.18/mysql-connector-java-5.1.18.jar
ADD module.xml /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main/

ADD enableProxy.xsl /opt/jboss/keycloak/

RUN java -jar /usr/share/java/saxon.jar \
	-s:/opt/jboss/keycloak/standalone/configuration/standalone.xml \
	-xsl:/opt/jboss/keycloak/enableProxy.xsl \
	-o:/opt/jboss/keycloak/standalone/configuration/standalone.xml;

RUN java -jar /usr/share/java/saxon.jar \
	-s:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml \
	-xsl:/opt/jboss/keycloak/enableProxy.xsl \
	-o:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml;

RUN rm /opt/jboss/keycloak/enableProxy.xsl

ENTRYPOINT [ "/opt/jboss/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0"]
