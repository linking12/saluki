FROM yingjunjiao/runtime-image:1.0
ENV TZ="Asia/Shanghai"
ENV LANG C.UTF-8
ADD saluki-gateway/target/saluki-gateway-1.5.3-SNAPSHOT.jar /root/app.jar
ADD bin/ /root/
RUN chmod +x /root/*.sh;mkdir /root/logs
ENV JAVA_OPTS ""
ENV APP_NAME saluki-gateway
WORKDIR /root
CMD ["./start.sh"]