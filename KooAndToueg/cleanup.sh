#!/bin/bash
netid=sxk159231
for i in `seq 1 44`
do
	if [ $i -lt 10 ]; 
	then
		host=$(echo dc0$i)
	else
		host=$(echo dc$i)
	fi
	ssh -o "StrictHostKeyChecking no" $netid@$host  pkill -f java -u sxk159231 &
	ssh -o "StrictHostKeyChecking no" $netid@$host  pkill -f sftp-server -u sxk159231 &
done
echo "Cleanup complete"
