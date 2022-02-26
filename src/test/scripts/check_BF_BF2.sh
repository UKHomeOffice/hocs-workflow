#!/bin/sh
# $1 - BF file inc path
# $2 - expected diffs directory

for f in $1
do

  # Get a BF2 equivalent
  bf2file=`echo $f | sed 's/BF_/BF2_/g'`

  if [ "$bf2file" == "$1" ]; then
    bf2file=`echo $1 | sed 's/BF/BF2/g'`
  fi

  # Build the diff output filename
  diffImage=`echo "$(basename $f)-$(basename $bf2file).diff" | sed 's/\.bpmn//g'`
  rm -f bfdiff.tmp
  # Adding the ls in so we can see any existing file does not exist
  ls bfdiff.tmp
  echo "Checking $f $bf2file...\c"
  # Create the new diff file
  diff --strip-trailing-cr $f $bf2file > bfdiff.tmp
  echo "Checking diff..."
  diff -b bfdiff.tmp $2/$diffImage
  exitCode=$?
  rm -f bfdiff.tmp
  if [ $exitCode -ne 0 ]; then
     echo "Unexpected error found, exited with exit code $exitCode"
     exit $exitCode;
  fi

  echo "OK"

done

# All good at this point
exit 0
