git stash
git checkout mergeScript
git pull
branches="branchNames"
while IFS= read - r line
do
git checkout "$line"
git merge master
done <"$file"
git checkout serverIpAddresses
