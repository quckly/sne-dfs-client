DFS Client application
===

The Kotlin client for DFS Master that mount FS by FUSE to some path.

## Build
```sh
./gradlew build -x test
```

## Run
TL;DR
```sh
java -jar build/libs/sne-dfs-client-0.0.1-SNAPSHOT.jar \
--app.client.mountpath=/tmp/mnt2 \
--app.master.address=http://127.0.0.1:10001 \
--app.requests.timeout=10000
```

##### Application params:
- `app.client.mountpath` - mount path
- `app.master.address` - address of master server in format: `http://ip:port`
- `app.requests.timeout` - timeout of http requestes to master in milliseconds

## Links:
Master server: https://github.com/quckly/sne-dfs-master
Storage server: https://github.com/wavvs/dfstorage
