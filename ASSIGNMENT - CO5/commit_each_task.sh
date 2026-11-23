#!/bin/sh
# Run once inside the repository folder after: git init && git remote add origin <your-github-url>
# Better still: run it one task at a time over the week so the commit dates differ.
git add README.md && git commit -m "Add README"
for t in A1 A2 A3 A4 A5 B1 B2 B3 B4; do
  git add $t && git commit -m "Task $t: add work and outputs"
done
git branch -M main && git push -u origin main
