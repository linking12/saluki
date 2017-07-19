FROM yingjunjiao/runtime-image:1.0
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
ADD saluki-example/saluki-example-client/target/saluki-example-client-1.5.2-SNAPSHOT.jar /root/app.jar
ADD bin/ /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV APP_NAME saluki-example-consumer
WORKDIR /root
CMD ["./start.sh"]