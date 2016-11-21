FROM java:8-jdk-alpine
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
RUN echo "http://mirrors.ustc.edu.cn/alpine/v3.4/main/" > /etc/apk/repositories;\
echo "http://mirrors.ustc.edu.cn/alpine/v3.4/community"  >> /etc/apk/repositories; \
apk update;
ADD saluki-example/saluki-example-client/build/libs/saluki-example-client-1.1-SNAPSHOT.jar /root/app.jar
ADD samoyed-web/bin /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV SERVER_PORT 8080
ENV APP_NAME saluki-example-consumer
WORKDIR /root
CMD ["./start.sh"]