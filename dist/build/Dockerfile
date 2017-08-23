FROM getzephyr/java8

MAINTAINER developer@getzephyr.com

RUN echo "*				soft	nofile			65536" >> /etc/security/limits.conf
RUN echo "*				hard	nofile			65536" >> /etc/security/limits.conf

#install log stash

RUN wget https://download.elastic.co/logstash/logstash/logstash-1.5.3.tar.gz && \
    tar xzf /logstash-1.5.3.tar.gz && \
    rm -f /logstash-1.5.3.tar.gz

ENV TOMCAT_VERSION 8.0.39

RUN wget --quiet --no-cookies http://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -O /tmp/catalina.tar.gz && \
	tar xzf /tmp/catalina.tar.gz -C /opt && \
	mv /opt/apache-tomcat-${TOMCAT_VERSION} /opt/tomcat && \
	rm /tmp/catalina.tar.gz && \
	rm -rf /opt/tomcat/webapps/* && \
	sed -i 's/connectionTimeout="20000"/connectionTimeout="60000"/g' /opt/tomcat/conf/server.xml && \
	wget --quiet --no-cookies http://archive.apache.org/dist/tomcat/tomcat-8/v${TOMCAT_VERSION}/bin/extras/catalina-jmx-remote.jar -O /opt/tomcat/lib/catalina-jmx-remote.jar && \
	apt-get update && \
	apt-get install -y libgcrypt11-dev && \
	apt-get clean all && \
	apt-get purge

RUN echo "networkaddress.cache.ttl=60" >> /usr/lib/jvm/java-8-oracle/jre/lib/security/java.security

RUN echo "America/Los_Angeles" > /etc/timezone && dpkg-reconfigure --frontend noninteractive tzdata

ADD tomcat/setenv.sh /opt/tomcat/bin/

ADD deploy/atlassian-connect.json /opt/tomcat/

ADD deploy/atlassian-connect.json /

ADD deploy/capture.war /opt/tomcat/webapps/

ENV CATALINA_HOME /opt/tomcat

ENV PATH $PATH:$CATALINA_HOME/bin



#defaults 
ENV SERVER_PORT 8080

EXPOSE ${SERVER_PORT}

# Start Tomcat

CMD /opt/tomcat/bin/catalina.sh start && tail -f /opt/tomcat/RUNNING.txt