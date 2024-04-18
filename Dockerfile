FROM openjdk:20

COPY proxy-1.0.0-SNAPSHOT.jar proxy.jar
COPY /configurations/ /configurations/
COPY /modules/ /modules/

ENTRYPOINT ["java","-jar","proxy.jar"]
