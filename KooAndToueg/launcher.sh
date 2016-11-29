#!/bin/bash


# Change this to your netid
netid=sxk159231


# Root directory of your project
PROJDIR=$HOME/CS6378/Project3/KooAndToueg/

CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/kootoueg

#
# Your main project class
#
PROG=Main


cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "\s*[0-9]\+\s*\w\+.*" |
(
	read line
	noNodes=$( echo $line | awk '{ print $1 } ')
	x=1
    while [ $x -le $noNodes ]; 
    do
		read line
		n=$( echo $line | awk '{ print $1 }' )
		host=$( echo $line | awk '{ print $2 }' )
		x=$((x+1))
	
		ssh -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $n &
    done
)



