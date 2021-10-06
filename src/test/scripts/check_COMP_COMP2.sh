# $1 - COMP file inc path
# $2 - expected diffs directory

for f in $1
do

  # Get a COMP2 equivalent
  comp2file=`echo $f | sed 's/COMP_/COMP2_/g'`

  if [ "$comp2file" == "$1" ]; then
    comp2file=`echo $1 | sed 's/COMP/COMP2/g'`
  fi

  # Build the diff output filename
  diffImage=`echo "$(basename $f)-$(basename $comp2file).diff" | sed 's/\.bpmn//g'`
  rm -f diff.tmp
  echo "Checking $f $comp2file...\c"
  diff $f $comp2file > diff.tmp
  diff -b diff.tmp $2/$diffImage
  exitCode=$?
  rm -f diff.tmp
  if [ $exitCode -ne 0 ]; then
     echo "Unexpected error found"
     exit $exitCode;
  fi

  echo "OK"

done

# All good at this point
exit 0

