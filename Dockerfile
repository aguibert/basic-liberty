FROM open-liberty:webProfile8
ADD build/libs/basic-liberty.war /config/dropins
COPY src/main/liberty/config /config/