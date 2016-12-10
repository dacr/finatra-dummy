#!/usr/bin/env bash

JAVA_OPTS=""
JAVA_OPTS=$JAVA_OPTS" -Xms2g"
JAVA_OPTS=$JAVA_OPTS" -Xmx2g"
JAVA_OPTS=$JAVA_OPTS" -Xmn1000m"
JAVA_OPTS=$JAVA_OPTS" -XX:+PerfDisableSharedMem"

#JAVA_OPTS=$JAVA_OPTS" -XX:+UseConcMarkSweepGC"
#JAVA_OPTS=$JAVA_OPTS" -XX:+UseParNewGC"
#JAVA_OPTS=$JAVA_OPTS" -XX:+CMSParallelRemarkEnabled"
#JAVA_OPTS=$JAVA_OPTS" -XX:+ScavengeBeforeFullGC"
#JAVA_OPTS=$JAVA_OPTS" -XX:+CMSScavengeBeforeRemark"
#JAVA_OPTS=$JAVA_OPTS" -XX:+ExplicitGCInvokesConcurrent"
#JAVA_OPTS=$JAVA_OPTS" -XX:+CMSClassUnloadingEnabled"
#JAVA_OPTS=$JAVA_OPTS" -XX:+UseCMSInitiatingOccupancyOnly"
#JAVA_OPTS=$JAVA_OPTS" -XX:CMSInitiatingOccupancyFraction=80"
JAVA_OPTS=$JAVA_OPTS" -XX:+UseAdaptiveSizePolicy"
#JAVA_OPTS=$JAVA_OPTS" -XX:ParallelGCThreads=4"
JAVA_OPTS=$JAVA_OPTS" -XX:+UseParNewGC"
JAVA_OPTS=$JAVA_OPTS" -XX:MaxGCPauseMillis=200"
JAVA_OPTS=$JAVA_OPTS" -XX:GCTimeRatio=95"

JAVA_OPTS=$JAVA_OPTS" -XX:+AggressiveOpts"
JAVA_OPTS=$JAVA_OPTS" -XX:+OptimizeStringConcat"
JAVA_OPTS=$JAVA_OPTS" -XX:+UseFastAccessorMethods"
JAVA_OPTS=$JAVA_OPTS" -XX:+UseThreadPriorities"
JAVA_OPTS=$JAVA_OPTS" -XX:ThreadPriorityPolicy=42"
JAVA_OPTS=$JAVA_OPTS" -verbose:gc"
JAVA_OPTS=$JAVA_OPTS" -XX:+PrintGCDetails"
JAVA_OPTS=$JAVA_OPTS" -XX:+PrintGCDateStamps"
JAVA_OPTS=$JAVA_OPTS" -Xloggc:GC_finatra.log"
JAVA_OPTS=$JAVA_OPTS" -Dcom.sun.management.jmxremote.port=2555"
JAVA_OPTS=$JAVA_OPTS" -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS=$JAVA_OPTS" -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS=$JAVA_OPTS" -Djava.net.preferIPv4Stack=true"
JAVA_OPTS=$JAVA_OPTS" -Djava.net.preferIPv6Addresses=false"
JAVA_OPTS=$JAVA_OPTS" -Djava.security.egd=file:///dev/urandom"
JAVA_OPTS=$JAVA_OPTS" -Dhazelcast.jmx=true"
JAVA_OPTS=$JAVA_OPTS" "

FINATRA_OPTS=""
FINATRA_OPTS=$FINATRA_OPTS" -tracingEnabled=false"
FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finagle.netty3.numWorkers=4"
FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.jvm.numProcs=8"
FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finagle.exp.scheduler=forkjoin:8"
#FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finagle.exp.scheduler=bridged:4"
#FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finagle.exp.scheduler=local"
#FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finagle.exp.scheduler=lifo"
FINATRA_OPTS=$FINATRA_OPTS" -com.twitter.finatra.config.maxRequestSize=500.megabytes"
FINATRA_OPTS=$FINATRA_OPTS" "

java $JAVA_OPTS -jar target/scala-2.11/dummy-finatra-1.0.jar $FINATRA_OPTS $*

