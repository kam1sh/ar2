# ar2
ar2 is a simple Python artifact repository with heavy focus on API.

# TL;DR
The only requirement is a JDK 8 and PostgreSQL, the rest is up to gradle:
```bash
./gradlew build
cp example-config.yaml ar2.yaml
nano ar2.yaml # change database settings
mkdir /tmp/packages
java -jar build/libs/ar2-all.jar createadmin --email admin@localhost --username admin
java -jar build/libs/ar2-all.jar serve
```

