#!/bin/sh

if [[ -n "$(git status -s | grep -v yarn.lock)" ]]; then
  echo "The working directory is dirty. Please commit any pending changes."
  exit 1;
fi

echo "Deleting old documentation"
rm -rf documentation
mkdir documentation
git worktree prune
rm -rf .git/worktrees/documentation/

echo "Checking out gh-pages branch into documentation"
git worktree add -B gh-pages documentation origin/gh-pages

echo "Removing existing files"
rm -rf documentation/*

echo "Generating documentation"
./node_modules/.bin/compodoc -p src/tsconfig.json

cd documentation
if [[ -n "$(git status -s)" ]] ; then
  echo "Updating gh-pages branch"
  git add --all && git commit -m "[ci skip] Publishing to gh-pages" && git push
fi
