#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers #
############################################################################################



echo "--- Copy DotaLeague_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/DotaLeague_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=5724672 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF
mkdir -p /var/scratch/yongguo/output/giraph_bfs/vs6_output_DotaLeague_FCF

for i in 1 2 3 4 5 #6 7 8 9 10
do
    echo "--- Run $i Stats for DotaLeague_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.BFSJob -D mapred.child.java.opts="-Xms3072m -Xmx3072m" undirected /local/hadoop.tmp.yongguo/DotaLeague_FCF /local/hadoop.tmp.yongguo/vs6_output_$i\_DotaLeague_FCF 120 0
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/vs6_output_$i\_DotaLeague_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_bfs/vs6_output_DotaLeague_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/vs6_output_$i\_DotaLeague_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

./stop-hadoop-cluster.sh
# --------------------------------------------------------------------------------------------