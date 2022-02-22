# create a diff image of the BF - BF2 diff

function displayUsage() {
  echo "Description: A script to create a diff file between a BF bpmn and the equivalent BF2 bpmn. The output"
  echo "             is then used in check_BF_BF2.sh as part of the Gradle test process."
  echo "Usage:"
  echo "BF_BF2_diff <source_file> <target_dir>"
  echo "  <source_file> - The BF bpmn file"
  echo "  <target_dir>  = The target directory to store the diff file"
}
# $1 - BF source file inc path
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
bf2file=`echo $1 | sed 's/BF_/BF2_/g'`

if [ "$bf2file" == "$1" ]; then
  bf2file=`echo $1 | sed 's/BF/BF2/g'`
fi

if [ ! -f $bf2file ]; then
	echo "$bf2file - doesn't exist!"
	exit 1;
fi

diffImage=`echo "$(basename $1)-$(basename $bf2file).diff" | sed 's/\.bpmn//g'`

diff --strip-trailing-cr $1 $bf2file > $2/$diffImage
echo "Created $2/$diffImage"
