# Java-Micro
A Java EE Maven project 

This includes:
* Maven for build system
* JUnit for unit testing
* Arquillian for Integration Testing
* Dockerized Wildfly testing 

## Dependencies
* Maven
* Docker
* Docker Compose (Linux).
 

## How to Run
Note that this being a typical JEE maven project, you may build and debug your web
application (via IDE or command line) without interacting with the remote application 
server.

```
$ mvn clean package
```

First, to build the docker images, you will need to run the following very time you run
the command above

```
$cd ../
```

```
$ docker-compose build
```

Then, launch the docker containers 
```
$ docker-compose up
```
This will run a containerized remote wildfly application server and corresponding support 
support services. 


You can redeploy the web application to the remote docker containerized AS issue the command within
the java-micro folder.

```
$ mvn clean install
```
Note that this command will run your JUnit tests before deploying the .war file to the 
application server.


## Updating variables

If you edit .envs, Dockerfile or docker-compose.yml files, stop, rebuild and restart the container:

```
$ docker-compose down

$ docker-compose up --build
```
