FROM open-liberty:microProfile2
ADD build/libs/myservice.war /config/dropins
COPY src/main/liberty/config /config/

EXPOSE 9080 9443