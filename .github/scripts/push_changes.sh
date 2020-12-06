#!/usr/bin/env bash
set -e
increment_version="$(dirname "$0")/increment_version.sh"
cd "$(dirname "$0")/../../" || exit 1
# REMOVE COVERAGE FILES
rm -rf coverage*
rm -rf codeclimate*
rm -rf ./*reporter*
# SETUP GIT CONFIG
git config --global user.name 'Kira'
git config --global user.email 'yuna-@web.de'
if [ -z ${1+x} ]; then
  echo "Warn remote url is not set"
else
  git remote set-url origin "$1"
fi
# UPDATE VERSION
if [[ $(git status --porcelain) ]]; then
  echo "Update version"
  "${increment_version}" -p
  echo "Pushing new git changes"
  git add .
  git commit -am "Updated dependencies"
  git push origin
else
  echo "No git changes to push"
fi
# CREATE TAG
if ./mvnw compile -P tag $ >/dev/null; then
  echo "New tag created"
  # new branch as trigger for new release
  git branch "release" || true
  git push origin --all -u || true
  git push origin --tags || true
else
  echo "Tag already exists"
fi
