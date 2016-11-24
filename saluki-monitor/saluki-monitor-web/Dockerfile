FROM yingjunjiao/runtime-image:1.0
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
ADD saluki-monitor/saluki-monitor-web/build/libs/saluki-monitor-web-1.1-SNAPSHOT.jar /root/app.jar
ADD saluki-monitor/saluki-monitor-web/bin /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV SERVER_PORT 9999
ENV APP_NAME saluki-monitor-web
WORKDIR /root
CMD ["./start.sh"]