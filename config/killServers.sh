screenName=('database' 'login' 'submission' 'answer' 'recognition' 'proxy' 'mongo')
cd ../CourseSketchProjects
length=$(expr ${#screenName[@]} - 1)
echo $length
for (( i=0; i<=$length; i++ ))
do
	#looks to see if there are multiple screens with the same name
	for session in $(screen -ls | grep -o "[0-9]*\.${screenName[$i]}")
	do
		screen -X -S "$session" quit
		echo "killing screen $session"
	done
done

echo 'sorry java you are going bye bye'
killall -9 java
#screen -S test -p 0 -X stuff 'command\n'