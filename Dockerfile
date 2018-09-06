FROM ubuntu:16.04
MAINTAINER Madeline Miller

RUN apt-get update \
  && apt-get -y install software-properties-common \
  && add-apt-repository ppa:linuxuprising/java \
  && apt-get update \
  && echo oracle-java10-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
  && apt-get -y install oracle-java10-installer

ADD CAB432Assignment1-*.jar app.jar
ADD photo_app.conf photo_app.conf

ENTRYPOINT java -jar app.jar