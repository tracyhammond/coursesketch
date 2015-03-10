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
git merge master
git push origin "$line"
done <"$branches"
git checkout "$oldbranch"
git stash apply
