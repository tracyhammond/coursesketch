#!/bin/bash

#clear old paths
echo "creating list of directories"
declare -a SERVERLIST
SERVERLIST[0]="CourseSketchDatabaseServer"
SERVERLIST[1]="coursesketchwebserver"
SERVERLIST[2]="CourseSketchRecognitionServer"
SERVERLIST[3]="BlankServer"
SERVERLIST[4]="CourseSketchSubmissionServer"
SERVERLIST[5]="CourseSketchAnswerCheckerServer"
SERVERLIST[6]="CourseSketchLoginServer"

echo "clearing old directories"
rm -rf output/  #deleting to replace it
rm -rf ../coursesketchwebclient/src/main/resources/other/protobuf/  #deleting
mkdir -p ../coursesketchwebclient/src/main/resources/other/protobuf/  #making
for server in "${SERVERLIST[@]}"; do
    echo "cleaning $server"
	rm -rf ../$server/src/protobuf/  #deleting
	rm -rf ../$server/reference/protobuf/  #deleting

	mkdir -p ../$server/src/protobuf/  #making
	mkdir -p ../$server/reference/protobuf/ #making
done

FILES=input/*
for f in $FILES; do
  # take action on each file. $f store current file name
  echo "Processing $f file..."
  FILENAME="$f"
  JUSTNAME="${FILENAME##*/}"

  DIR="${JUSTNAME%.*}"

  mkdir -p output/cpp/$DIR/
  mkdir -p output/java/$DIR/
  mkdir -p output/py/$DIR/
  echo "putting files in output/language/$DIR"
  protoc --cpp_out=output/cpp/$DIR/ --java_out=output/java/$DIR/ --python_out=output/py/$DIR/ $f

  echo "copying files to coursesketchwebclient/other/"
  cp -f $f ../coursesketchwebclient/other/protobuf/  #copying

  #copy all of the server protos
  for server in "${SERVERLIST[@]}";
  do
    echo "copying java files to $server/src/"
    cp -r -f output/java/$DIR/ ../$server/src/ #copying java
    cp -r -f $f ../$server/reference/protobuf/  #copying reference
  done

  #javac -cp "protobuf-2.5.0.jar" -d "output/java/$DIR/" -sourcepath output/java/$DIR/srl/ *.java
  #echo "creating compiled java files"
  mkdir -p jars
  cd jars
  jar cf $DIR.jar -C ../output/java/$DIR .
  echo "creating jar files they are located in jars"
  cd ..
  echo ""
done