#!/bin/bash
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
  cp -f $f ../coursesketchwebclient/other/

  echo "copying java files to coursesketchwebserver/src/"
  cp -r -f output/java/$DIR/ ../coursesketchwebserver/src/

  echo "copying java files to CourseSketchRecognitionServer/src/"
  cp -r -f output/java/$DIR/ ../CourseSketchRecognitionServer/src/

  #javac -cp "protobuf-2.5.0.jar" -d "output/java/$DIR/" -sourcepath output/java/$DIR/srl/ *.java
  #echo "creating compiled java files"
  mkdir -p jars
  cd jars
  jar cf $DIR.jar -C ../output/java/$DIR .
  echo "creating jar files they are located in jars"
  cd ..
  echo ""
done