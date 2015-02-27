git stash
git checkout mergeScript
git pull
branches="branchNames"
while IFS= read -r line
do
git checkout "$line"
git merge mergeScript
git push origin "$line"
done <"$branches"
git checkout mergeScript
