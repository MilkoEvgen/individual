FROM openjdk:21-jdk

WORKDIR /usr/local/lib

COPY ./target/*.jar /usr/local/lib/app.jar

COPY ./src/main/resources/ngnix_cert.crt /usr/local/share/ca-certificates/ngnix_cert.crt

RUN keytool -import -trustcacerts -file /usr/local/share/ca-certificates/ngnix_cert.crt -alias ngnix_cert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

EXPOSE 8081

CMD ["java", "-jar", "/usr/local/lib/app.jar"]

