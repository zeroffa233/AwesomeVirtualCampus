#!bin/bash
int=1
while (($int <= 100)); do
  touch test
  git add .
  git commit -m "Test touch $int."
  rm test
  git add .
  git commit -m "Test remove $int."
  let "int++"
done
git push
