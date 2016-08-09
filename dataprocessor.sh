#!/bin/bash

csv_folder=bcrssirecords
adb_remote=/sdcard/"$csv_folder"/
csv_ext=.csv

contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "${string}"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

process_file() {
	postfile="${1%.*}"_post.csv
	echo "Seconds,Time,Rssi,Enter/Exit">"${postfile}"
	previewline=""
	linecnt=0

	while read line
	do

		if test ${#previewline} == 0
		then
			echo "first line"
			previewline="${line}"
		else
			time1=`echo "${previewline}"|cut -d ',' -f1`
			time2=`echo "${line}"|cut -d ',' -f1`
			timediff=$((${time2}-${time1}))

			echo "${previewline}">>"${postfile}"

			loopCnt=1
			previousEnter=`echo "${previewline}"|cut -d ',' -f4`
			while [ ${loopCnt} -lt ${timediff} ]
			do
				time3=$((${time1}+${loopCnt}))
				timestr=`date -jr ${time3} +"%m.%d %H:%M:%S"`
				line2=`echo "${time3},${timestr},0,${previousEnter}"`
				echo "${line2}">>"${postfile}"
				loopCnt=$((${loopCnt}+1))
			done

			# if test "${timediff}" != "1"
			# then
			# 	echo "${linecnt}"--"${time1}"--${timediff}
		 #    fi

			previewline="${line}"
		fi
		let "linecnt++"
	done < "$1"
	previewline=""
	linecnt=0
}

adb pull ${adb_remote} .

for entry in "$csv_folder"/*
do
	echo ${entry}
	poststr="${entry#*post.csv}"

	if test "${#poststr}" != "0" 
	then 
		process_file ${entry}
	fi
	
done

# iii=1470380565
# # ((sec=i%60, i/=60, min=i%60, i/=60, hrs=i%60))
# timestr=`date -jr ${iii} +"%m.%d %H:%M:%S"`
# echo "${timestr}"

# timestamp=$(printf "%d:%02d:%02d" $hrs $min $sec)
# echo $timestamp





