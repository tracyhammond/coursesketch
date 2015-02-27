git stash
git checkout mergeScript
git pull
branches="branchNames"
cd ..
while IFS= read - r line
do
git checkout "$line"
git merge mergeScript
done <"$file"
git checkout mergeScriptTest
