FROM maven:3.6.1-jdk-8 AS build
ARG BASE=/usr/src/editor
COPY pom.xml ${BASE}/
COPY .bowerrc ${BASE}/
COPY bower.json ${BASE}/
COPY Gruntfile.js ${BASE}/
COPY .jshintrc ${BASE}/
COPY package.json ${BASE}/
COPY .yo-rc.json ${BASE}/
COPY src ${BASE}/src

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.3/install.sh | bash \
 && export NVM_DIR="$HOME/.nvm" \
 && [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" \
 && [ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion" \
 && nvm install 8.11.2 \
 && npm install -g npm \
 && npm install -g bower grunt-cli \
 && mvn -f ${BASE}/pom.xml clean package -DskipTests

FROM openjdk:8-jdk-alpine
COPY --from=build /usr/src/editor/target/editor-*.war /app/editor.war

EXPOSE 8080
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /app/editor.war --spring.datasource.serverName=db --spring.datasource.username=$POSTGRES_USER --spring.datasource.password=$POSTGRES_PASSWORD --application.links.engine.port=$ENGINE_PORT --application.links.engine.host=$ENGINE_HOST

