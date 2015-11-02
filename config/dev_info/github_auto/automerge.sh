#!/bin/sh
git stash clear
git stash
oldbranch=$(git rev-parse --abbrev-ref HEAD)
git checkout master
git pull
branches="branchNames"
while IFS= read -r line
do
git checkout "$line"
git pull origin "$line"
git merge master
success=$?
if [[ $success -eq 0 ]];
then
    git push origin "$line"
else
    echo "Merging branch $line Failed"
    git checkout "$oldbranch"
    exit 1
fi
done <"$branches"
git checkout "$oldbranch"
git stash apply

exit 0
