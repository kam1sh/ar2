# ar2
ar2 is a simple Python artifact repository with heavy focus on API.

# TL;DR
The only requirement is a JDK 11 and PostgreSQL, the rest is up to gradle:
```bash
./gradlew shadowJar
cp example-config.yaml ar2.yaml
nano ar2.yaml # change database settings
mkdir /tmp/packages
java -jar build/libs/ar2-all.jar createadmin --email admin@localhost --username admin
java -jar build/libs/ar2-all.jar serve
```

### Memory limits
You may use systemd resource control support:
```bash
sudo systemd-run -t -p WorkingDirectory=$PWD -p MemoryMax=256M /usr/bin/java -jar build/libs/ar2-all.jar serve
```

# API usage
I recommend to use [httpie](https://github.com/jakubroztocil/httpie).
```
http -j -v ':8080/api/v1/login' 'Content-Type:application/json; charset=UTF-8' 'username=admin' 'password=123'
```
