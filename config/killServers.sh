screenName=('database' 'login' 'submission' 'answer' 'recognition' 'proxy' 'mongo')
cd ../CourseSketchProjects
length=$(expr ${#screenName[@]} - 1)

# gets all of the java processes and their grand parents
# the reason is that the screen makes 2 processes one is the java process and the other is the parent process
# I can't grab a children in mac for some reason BUT i can grab the parent process
javaPs=()
javaGpPs=()
for javaId in $(pgrep java)
do
	#echo
	#echo $javaId
	#echo $(ps -o ppid= $javaId)
	#echo $(ps -o ppid= $(ps -o ppid= $javaId))
	javaPs+=($javaId)
	javaGpPs+=($(ps -o ppid= $(ps -o ppid= $javaId)))
done

echo "Child procressed followed by screen processes"
echo ${javaPs[@]}
echo ${javaGpPs[@]}
#gets the index of an element in an array
#search term is first followed by the array
#note that becuase it returns by echo you can not add any debug statements into this function
search() {
    local i=1;
	searchTerm="${1}"
	shift #moves over the argument looking
	array=("${@}") #grabs the rest of the args as an array (which is an array)
    for str in ${array[@]}; do
        if [ "$str" = "$searchTerm" ]; then
            echo $((i - 1)) #should reference the correct index (0 to something)
            return
        else
            ((i++))
        fi
    done
    echo "-1"
}

for (( i=0; i<=$length; i++ ))
do
	#looks to see if there are multiple screens with the same name
	for session in $(screen -ls | grep -o "[0-9]*\.${screenName[$i]}")
	do
		echo
		echo "killing screen $session"
		IFS='.' read -ra ADDR <<< "$session" #splits the id from the name
		pid=${ADDR[0]}
		screen -X -S "$session" quit # exit session

		# now we kill the still running java process (because it will not exist for some reason)
		itemIndex=$(echo $(search "${pid}" "${javaGpPs[@]}"))
		javaId=${javaPs[$itemIndex]}
		# the process that is being killed
		echo "killing java process"
		echo $(ps -p $javaId)
		kill -9 $javaId
		sleep 1
	done
done

echo
echo "All process should now be dead doing extra clean up now"
screen -wipe #remove all dead screens
