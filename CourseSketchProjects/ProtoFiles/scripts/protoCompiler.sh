#!/bin/bash

#clear old paths

echo "clearing old directories"
WEBDIR="../../coursesketchwebclient/src/main/resources/other/protobuf/"
rm -rf output/  #deleting to replace it
rm -rf $WEBDIR  #deleting
mkdir -p $WEBDIR  #making

FILES=../input/*
for f in $FILES; do
  # take action on each file. $f store current file name
  echo "Processing $f file..."
  FILENAME="$f"
  JUSTNAME="${FILENAME##*/}"

  DIR="${JUSTNAME%.*}"

  echo "copying files to $WEBDIR"
  cp -f $f $WEBDIR  #copying
done