#!/bin/bash
module load spark/2.2.0
hdfs dfs -rm -r "loudacre/noiseresult"

nohup spark-shell -i generateLatLngtoNoiseCount.scala >run.log 2>&1 &

