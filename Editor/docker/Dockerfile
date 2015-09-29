FROM java:8
VOLUME /tmp

ADD editor-0.0.1-SNAPSHOT.war app.war
RUN bash -c 'touch /app.war'

CMD java -Djava.security.egd=file:/dev/./urandom -jar /app.war --spring.profiles.active=prod --spring.datasource.serverName=db --spring.datasource.username=$POSTGRES_USER --spring.datasource.password=$POSTGRES_PASSWORD --application.links.engine=$ENGINE_LINK

