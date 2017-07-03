#!/usr/bin/env bash

#install google app engine using
#install anaconda and create an environment called py27
#if you are using python3 for other things
# sudo apt-get install google-cloud-sdk-app-engine-python
source activate py27
cd ./../CourseSketchProjects/coursesketchwebclient
dev_appserver.py app.yaml &
source deactivate

mongod &
