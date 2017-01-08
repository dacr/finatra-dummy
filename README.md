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


## Some requirements

```
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 elasticsearch
```

## tuning

* Use java 8
* System tuning :
  ```
  sysctl -w net.ipv4.ip_local_port_range="5000 65535"
  sysctl -w net.ipv4.tcp_tw_reuse=0
  sysctl -w net.ipv4.tcp_tw_recycle=0
  ```
* Elasticsearch system mandatory requirements :  
  `sysctl -w vm.max_map_count=262144`


## Some various notes 

* for finatra :
  + Take care only use Twitter future not scala ones, only twitter ones are recognized for responses.
    - conversion is quite easy thanks to [twitter bijection](https://github.com/twitter/bijection/blob/develop/README.md)
* some usefull elasticsearch command :
  + `curl -s 'http://localhost:9200/_search?q=*' | jq`
  + `curl -s 'http://localhost:9200/_search?q=*&size=100' | jq`
  + `curl -XPOST 'http://localhost:9200/dummy/basic' -d '{ "message" : "hello" }'`


  