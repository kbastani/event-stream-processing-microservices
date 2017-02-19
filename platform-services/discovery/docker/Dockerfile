FROM anapsix/alpine-java:8
VOLUME /tmp
ADD https://drive.google.com/file/d/0B4_9rQ6N7z52NVFZRVJNbU55ZWM/view?usp=sharing app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
