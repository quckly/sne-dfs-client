DFS Client application
===

The Kotlin client for DFS Master that mount FS by FUSE to some path.

## Build
You should have Java JDK and FUSE kernel module
```sh
sudo apt install openjdk-8-jdk
```
Build
```sh
./gradlew build -x test
```

## Run
Start DFS client and mount FS to /tmp/mnt
```sh
# Make sure /tmp/mnt is created
mkdir /tmp/mnt

java -jar build/libs/sne-dfs-client-0.0.1-SNAPSHOT.jar \
--app.client.mountpath=/tmp/mnt \
--app.master.address=http://127.0.0.1:10001 \
--app.requests.timeout=10000
```

##### Application params:
- `app.client.mountpath` - mount path
- `app.master.address` - address of master server in format: `http://ip:port`
- `app.requests.timeout` - timeout of http requestes to master in milliseconds

## Links:
Master server: https://github.com/quckly/sne-dfs-master 

Client application: https://github.com/quckly/sne-dfs-client 

Storage server: https://github.com/wavvs/dfstorage 
