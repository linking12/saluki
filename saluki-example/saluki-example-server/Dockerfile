FROM yingjunjiao/runtime-image:1.0
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
ADD saluki-example/saluki-example-server/build/libs/saluki-example-server-1.5.1.jar /root/app.jar
ADD saluki-example/saluki-example-server/bin /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV APP_NAME saluki-example-provider
WORKDIR /root
CMD ["./start.sh"]