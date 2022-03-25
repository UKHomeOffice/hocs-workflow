#!/bin/sh
# create a diff image of the COMP - COMP2 diff

function displayUsage() {
  echo "Description: A script to create a diff file between a COMP bpmn and the equivalent COMP2 bpmn. The output"
  echo "             is then used in check_COMP_COMP2.sh as part of the Gradle test process."
  echo "Usage:"
  echo "COMP_COMP2_diff <source_file> <target_dir>"
  echo "  <source_file> - The COMP bpmn file"
  echo "  <target_dir>  = The target directory to store the diff file"
}
# $1 - COMP source file inc path
# $2 - target dir path for output

if [ -z "$1" ]; then
  displayUsage
  exit 1;
fi

if [ -z "$2" ]; then
  displayUsage
  exit 1;
fi


# Build the diff output filename
comp2file=`echo $1 | sed 's/COMP_/COMP2_/g'`

if [ "$comp2file" == "$1" ]; then
  comp2file=`echo $1 | sed 's/COMP/COMP2/g'`
fi

if [ ! -f $comp2file ]; then
	echo "$comp2file - doesn't exist!"
	exit 1;
fi

diffImage=`echo "$(basename $1)-$(basename $comp2file).diff" | sed 's/\.bpmn//g'`

diff -bw $1 $comp2file > $2/$diffImage
echo "Created $2/$diffImage"
