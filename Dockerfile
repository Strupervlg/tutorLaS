FROM openjdk:17-oracle

WORKDIR /backend
COPY . .
EXPOSE 3000
ENTRYPOINT ["java","-jar","app.jar"]