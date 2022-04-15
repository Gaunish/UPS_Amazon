FROM openjdk:11
RUN apt-get update
RUN apt-get install -y make 

FROM gradle:4.4.1
ADD --chown=gradle . /backend
WORKDIR /backend
RUN ./gradlew installDist

CMD ["./app/build/install/app/bin/app"]