#!/bin/bash

#clear old paths
echo "clearing old directories"
rm -rf output/  #deleting to replace it
rm -rf ../coursesketchwebclient/other/protobuf/  #deleting
rm -rf ../coursesketchwebserver/src/protobuf/  #deleting
rm -rf ../CourseSketchRecognitionServer/src/protobuf/  #deleting

rm -rf ../coursesketchwebserver/reference/protobuf/  #deleting
rm -rf ../CourseSketchRecognitionServer/reference/protobuf/  #deleting

mkdir -p ../CourseSketchRecognitionServer/reference/protobuf/
mkdir -p ../coursesketchwebserver/reference/protobuf/

FILES=input/*
for f in $FILES

do
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

  mkdir -p ../coursesketchwebclient/other/protobuf/  #making
  cp -f $f ../coursesketchwebclient/other/protobuf/  #copying

  echo "copying java files to coursesketchwebserver/src/"
  mkdir -p ../coursesketchwebserver/src/protobuf/  #making
  cp -r -f output/java/$DIR/ ../coursesketchwebserver/src/ #copying java
  cp -r -f $f ../coursesketchwebserver/reference/protobuf/  #copying reference

  echo "copying java files to CourseSketchRecognitionServer/src/"

  mkdir -p ../CourseSketchRecognitionServer/src/protobuf/  #making
  cp -r -f output/java/$DIR/ ../CourseSketchRecognitionServer/src/ #copying java
  cp -r -f $f ../CourseSketchRecognitionServer/reference/protobuf/  #copying reference

  #javac -cp "protobuf-2.5.0.jar" -d "output/java/$DIR/" -sourcepath output/java/$DIR/srl/ *.java
  #echo "creating compiled java files"
  mkdir -p jars
  cd jars
  jar cf $DIR.jar -C ../output/java/$DIR .
  echo "creating jar files they are located in jars"
  cd ..
  echo ""
done