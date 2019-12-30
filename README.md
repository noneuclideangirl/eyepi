# EyePi: an interactive service monitor
EyePi is designed to keep track of the microservices I have running on my Raspberry Pi, and to allow
me to remotely start, stop, and modify their metadata. I started this project to learn more about my
employer's tech stack, in particular using React, and also because I was sick of having to ssh into
my pi every time something went wrong to figure out what happened. I plan to extend this project to
notify me when a service dies unexpectedly, because I don't have any infrastructure to support that
on my pi as currently set up. 

## Overview
The Java server connects to a Mongo database of services, and starts each one. It then listens for
commands, either as raw JSON or as HTTP requests containing a JSON body.

### Java backend
The `Server` class creates a `CommandHandler` and listens for socket connections. When it receives a
connection, it creates a `ClientHandler` that waits to receive commands. Each `ClientHandler` sends
the command to the `CommandHandler`, which produces a response, and then writes the response to the
socket.

There is a `ServiceMonitor` singleton which spawns and monitors processes using bash commands,
and a `DatabaseManager` singleton which presents an interface to the database. 

## Build instructions
The Java backend is located in `src/main/java/net/noneuclideangirl`. 
To create the folders used for storing logs etc. as well as a base config file, run `setup.sh`.
Building via Maven (`mvn package`) in the root folder will build the server jar; simply run
with `java -jar target/eye-pi-1.0-SNAPSHOT.jar`.

The React frontend is located in `client`. To build and run, use `npm start`. 
