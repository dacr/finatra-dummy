# finatra-dummy

This a dummy project, to play with various technologies :
* [finatra](http://twitter.github.io/finatra/)
* [bootstrap](http://getbootstrap.com/)
* [highcharts & highstock](http://www.highcharts.com/) for timeseries
* [dropzone](http://www.dropzonejs.com/)
* [elasticsearch](https://www.elastic.co/fr/products/elasticsearch)
* [elastic4s](https://github.com/sksamuel/elastic4s)

## quick starts

- `sbt run` starts the server which listens on `0.0.0.0:8888`

```
git clone https://github.com/dacr/finatra-dummy.git
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
