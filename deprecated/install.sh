#!/bin/bash

DEVICE=""

if [ "$1" != "" ]
then
	DEVICE="$1"
fi

echo " #### adb devices #### "
adb devices

echo " #### push apk #### "
adb $DEVICE push ./app-release.apk /data/local/tmp/tw.firemaples.onscreenocr

echo " #### install apk #### "
adb $DEVICE shell pm install -r "/data/local/tmp/tw.firemaples.onscreenocr"

echo " #### launch app #### "
adb $DEVICE shell am start -n "tw.firemaples.onscreenocr/tw.firemaples.onscreenocr.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
