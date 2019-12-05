# kvm-updater

This is a prototype implementation to update [CloudStack](https://cloudstack.apache.org/) KVM host automatically.
To run this project you need an eighter Maven or Docker.

## Docker Build

Befor running any docker image you need to build it first. To build this project run below command
in root directory of project.

```
$ docker build -t kvm-updater --build-arg build_target=kvm-updater .
```

After successfull build, run it with below command:

```
$ docker run -it -v $(pwd)/conf/:/app/conf/:ro -v $(pwd)/logs/:/app/logs/ kvm-updater
```

## Maven Build

You can also build and run this project with Maven as follow:

**build**:
```
$ mvn install
```
**Run**:
```
$ java -jar target/kvm-updater.jar
```
**Run**: (with a customized log configuration file)
```
$ java -Dlog4j.configurationFile=./log4j2.xml -jar target/kvm-updater.jar
```
