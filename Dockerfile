FROM maven:3.8-jdk-8 AS MAVEN_BUILD
LABEL maintainer="1500418656@qq.com"
WORKDIR /build/
#COPY pom.xml .
#COPY settings.xml /root/.m2/
COPY pom.xml .
RUN mvn clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r target/
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true
FROM openjdk:8
WORKDIR /root
EXPOSE 8080
EXPOSE 8081
CMD mkdir /data/static/upload/
COPY src/main/resources/image /data/static/upload/image
COPY src/main/resources/dict.txt /data/static/dict.txt
COPY --from=MAVEN_BUILD /build/target/xdu-partner-0.0.2-SNAPSHOT.jar /root/xdu-partner.jar
ENTRYPOINT ["java","-jar", "xdu-partner.jar"]
