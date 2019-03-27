## Run the unittests against other databases using Docker containers

### PostgreSQL

...
docker run -p 5433:5432/tcp -e POSTGRES_PASSWORD=syndesis -d postgres
psql -h localhost -p 5433 -U postgres

psql>create database test;
...

### MySQL

...
docker run -p 3307:3306/tcp -e MYSQL_ROOT_PASSWORD=syndesis -d mysql
mysql -p3307 -Uroot -p
mysql> create database test;
...

### Oracle

...
git clone https://github.com/oracle/docker-images.git
cd docker-images/OracleDatabase/SingleInstance/dockerfiles
...

Download the 4.3Gb(!) 18.3.0 Linux x86-64 zipfile from 
http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html

This makes you accept the license agreement and it also makes you login to the OTN.
Move the zipfile into the 18.3.0 directory.

Now you are ready to create and run the Docker image using
...
./buildDockerImage.sh -v 18.3.0 -s

docker run \
-p 1521:1521 -p 5500:5500 \
-e ORACLE_SID=syndesis \
-e ORACLE_PDB=test \
-e ORACLE_PWD=syndesis \
oracle/database:18.3.0-se2

docker exec -ti <container name> sqlplus pdbadmin@ORCLPDB1
...

For more info see https://github.com/oracle/docker-images/blob/master/OracleDatabase/SingleInstance/README.md#running-oracle-database-18c-express-edition-in-a-docker-container