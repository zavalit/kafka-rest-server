FROM ubuntu:14.04

ENV LAST-UPDATE 25.02.2016
MAINTAINER Seva Dolgopolov

ENV JAVA_VERSION_MAJOR 8
ENV JAVA_VERSION_MINOR 74
ENV JAVA_VERSION_BUILD 02
ENV JAVA_PACKAGE       jre
ENV KAFKA_VER 0.9.0.1
ENV SCALA_VER 2.11
ENV SERVER_VER 0.1


# Install cURL
RUN apt-get update && apt-get install -y wget curl ca-certificates zookeeper && \
    rm -rf /var/lib/apt/lists/* 

# Download and unarchive Java
RUN mkdir /opt/java && curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie"\
  http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/${JAVA_PACKAGE}-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
    | tar -xzf - -C /opt/java &&\
    ln -s /opt/java/${JAVA_PACKAGE}1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} /opt/$JAVA_PACKAGE &&\
    rm -rf /opt/jdk/*src.zip

# Set environment
ENV JAVA_HOME /opt/$JAVA_PACKAGE
ENV PATH ${PATH}:${JAVA_HOME}/bin

RUN  wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VER"/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz -O /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz && \
    tar xfz /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz -C /opt && \
    rm /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz

ENV KAFKA_HOME /opt/kafka_"$SCALA_VER"-"$KAFKA_VER"
ADD kafka-start /usr/local/bin/kafka-start
ADD target/scala-$SCALA_VER/kafka-rest-server-assembly-$SERVER_VER.jar /root/
RUN chmod +x /usr/local/bin/kafka-start

CMD kafka-start --with-server 
