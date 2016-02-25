FROM zavalit/java-jre

MAINTAINER Seva Dolgopolov
ENV LAST-UPDATE 25.02.2016
ENV KAFKA_VER 0.9.0.1
ENV SCALA_VER 2.11
ENV SERVER_VER 0.1

RUN apt-get install -y zookeeper wget && \
    rm -rf /var/lib/apt/lists/* && \
    wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VER"/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz -O /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz && \
    tar xfz /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz -C /opt && \
    rm /tmp/kafka_"$SCALA_VER"-"$KAFKA_VER".tgz


ENV KAFKA_HOME /opt/kafka_"$SCALA_VER"-"$KAFKA_VER"
ADD kafka-start /usr/local/bin/kafka-start
ADD target/scala-$SCALA_VER/kafka-rest-server-assembly-$SERVER_VER.jar /root/
RUN chmod +x /usr/local/bin/kafka-start

CMD kafka-start --with-server 
