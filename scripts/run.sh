#!/bin/sh

echo 
echo " Starting Report Out"

cd /home/csyperski/programming/netbeans-workspace/ReportOutBackEnd.git

time mvn clean package
cd target
time java -jar ReportOut.jar --spring.config.location=$HOME/ReportOutData/application.properties

cd ..;

echo " Done"
echo 
