FROM open-liberty:microProfile2
ADD build/libs/basic-liberty-1.0-SNAPSHOT.war /config/apps
COPY src/main/liberty/config /config/

EXPOSE 9080 9443