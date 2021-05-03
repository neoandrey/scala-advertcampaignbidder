ARG OPENJDK_TAG=8u232-jre-alpine

FROM openjdk:${OPENJDK_TAG}
MAINTAINER Bolaji Aina <neoandey@yahoo.com>

ENV SBT_VERSION=1.4.7 \
    SCALA_VERSION=2.13.4 \
    SCALA_HOME=/usr/share/scala

ARG APP_PORT
ARG APP_NAME
ARG APP_REPO
ARG ACS_TOKEN

WORKDIR /opt/scala/$APP_NAME

RUN apk add --no-cache  \
    jq    \
    unzip \
    tar   \
    wget  \
    git   \
    bash  \
    &&  cd /tmp \
    &&  wget "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" \
    &&  git clone "${APP_REPO}" \
    &&  tar -xzf "/tmp/sbt-${SBT_VERSION}.tgz"  \
    &&  cp  /tmp/sbt/bin/* /usr/local/bin  \
    &&  mkdir -p  /opt \
    &&  mkdir -p  /opt/scala \
    &&  mkdir -p  /opt/scala/$APP_NAME \
    &&  mkdir -p  /opt/scala/$APP_NAME/logs \
    &&  cp -r /tmp/$APP_NAME/* /opt/scala/$APP_NAME/ \
    &&  cd /opt/scala/$APP_NAME \
    &&  rm -rf /tmp/*  \
    &&  sbt clean compile

EXPOSE $APP_PORT

CMD  sbt run