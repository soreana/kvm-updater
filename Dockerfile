FROM maven as build

COPY . /app/

WORKDIR /app

RUN mvn install

CMD /bin/sh

FROM openjdk:8-jre-alpine

ARG build_target=raha
ENV target=$build_target
ENV args=""

COPY --from=build /app/target/$build_target.jar /app/

WORKDIR /app

ENTRYPOINT java -Dlog4j.configurationFile=conf/log4j2.xml -jar $target.jar -c conf/$target-conf.properties $args

