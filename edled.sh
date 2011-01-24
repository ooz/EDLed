#!/bin/bash

OS=`uname`

if [ $OS = "Darwin" ]; then
    java -cp .:plugin/:res/lib/ -Xdock:name="EDLed" -Xdock:icon=res/img/edled.png -jar edled.jar $1
else
    java -cp .:plugin/:res/lib/ -jar edled.jar $1
fi

