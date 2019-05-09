FROM ubuntu:18.04
MAINTAINER Murashko Ilya
RUN apt-get update -y
ENV PGVER 10
RUN apt-get install -y postgresql-$PGVER
USER postgres

RUN /etc/init.d/postgresql start &&        psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" && createdb -E UTF8 -T template0 -O docker forumdb && /etc/init.d/postgresql stop

RUN echo "local all all trust" >> /etc/postgresql/$PGVER/main/pg_hba.conf
RUN echo "host  all all 127.0.0.1/32 trust" >> /etc/postgresql/$PGVER/main/pg_hba.conf

RUN echo "host  all all ::1/128 trust" >> /etc/postgresql/$PGVER/main/pg_hba.conf
RUN echo "host  all all 0.0.0.0/0 trust" >> /etc/postgresql/$PGVER/main/pg_hba.conf

RUN cat /etc/postgresql/$PGVER/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "synchronous_commit = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "temp_buffers = 128MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "work_mem = 64MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "maintenance_work_mem = 128MB" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "log_statement = 'none'" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN sed -i.bak -e 's/shared_buffers.*/shared_buffers = 800MB/' /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "fsync = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "synchronous_commit = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "full_page_writes = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
EXPOSE 5432

VOLUME /etc/postgresql /var/log/postgresql /var/lib/postgresql
USER root
RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven

ENV WORK /opt/
ADD / $WORK/
WORKDIR $WORK
RUN mvn package

EXPOSE 5000

CMD service postgresql start && java -Xms200M -Xmx200M -Xss256K -jar target/DB-1.0-SNAPSHOT.jar