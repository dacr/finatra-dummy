# primesui-finatra

## quick starts

- `export PRIMESUI_CACHE=true` to enable second level cache
- `sbt run` starts the server which listens on `0.0.0.0:8888`

```
git clone 
export PRIMESUI_CACHE=true
sbt run
```

## run in place in developer mode
As soon as something changes, finatra restarts...

```
sbt '~re-start'
sbt '~re-start -maxRequestSize=500.megabytes'
```

ps : brought to us thanks to https://github.com/spray/sbt-revolver sbt plugin

## Packaging and quick in-place run

```
sbt assembly
./runit.sh
```

`./runit.sh -help` to view options.

## tuning to achieve best results

- Use java 8
- System tuning :
  ```
  sysctl -w net.ipv4.ip_local_port_range="5000 65535"
  sysctl -w net.ipv4.tcp_tw_reuse=0
  sysctl -w net.ipv4.tcp_tw_recycle=0
  ```


## For quick load test using primesui-loadtests project

```
git clone https://github.com/dacr/primes-scalatra-app-loadtests.git
cd primes-scalatra-app-loadtests
export PRIMESUI_LOADTEST_DURATION=3
export PRIMESUI_URL=http://127.0.0.1:8888/
export PRIMESUI_VUS=25000
sbt test
```

## Results hints

with good hardware => ~34000 hit/s

