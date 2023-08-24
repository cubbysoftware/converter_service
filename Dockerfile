#  ---------------------------------- debian ------
FROM debian:latest
LABEL maintainer="Cubby Software <aolmez@cubbysoftware.com>"
# FROM debian:sid

# debian:stable with LibreOffice 7.0.4.2 00(Build:2) 17-19 millis/500 pdf's
# debian:sid    with LibreOffice 7.4.5.1 40(Build:1) 18-20 millis/500 pdf's
# performance is nearly the same (by 2023-03), stable image is a bit smaller > stable

#  ---------------------------------- libreoffice ------
RUN apt-get update && apt-get -y install \
        openjdk-17-jre \
        apt-transport-https locales-all libpng16-16 libxinerama1 libgl1-mesa-glx libfontconfig1 libfreetype6 libxrender1 \
        libxcb-shm0 libxcb-render0 adduser cpio findutils \
        # procps needed for us finding the libreoffice process, see https://github.com/sbraconnier/jodconverter/issues/127#issuecomment-463668183
        procps \
    # sid variant
    && apt-get -y install libreoffice --no-install-recommends \
    # fonts
    && apt-get -y install fonts-liberation \
    && rm -rf /var/lib/apt/lists/*

#  ---------------------------------- spring boot app ------

ARG JAR_FILE=target/*.jar

# cd /opt/app
WORKDIR /opt/app

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

EXPOSE 1083

# java -jar /opt/app/app.jar
ENTRYPOINT ["/usr/bin/java","-jar","app.jar"]