FROM yingjunjiao/runtime-image:1.0
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
RUN echo "http://mirrors.ustc.edu.cn/alpine/v3.4/main/" > /etc/apk/repositories;\
echo "http://mirrors.ustc.edu.cn/alpine/v3.4/community"  >> /etc/apk/repositories; \
apk update;
ADD saluki-monitor/saluki-monitor-web/build/libs/saluki-monitor-web-1.1-SNAPSHOT.jar /root/app.jar
ADD saluki-monitor/saluki-monitor-web/bin /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV SERVER_PORT 8181
ENV APP_NAME saluki-example-provider
WORKDIR /root
CMD ["./start.sh"]