# atom-treecommit

Commit, stage, stash and push directly from the treeview.

Based loosely on Emacs' amazing Magit.

## Goals

- Keep your fingers on the keyboard
- Be able to stage chunks easily
- Be able to create precise commits quickly
- Allow most simple operations from the treeview
- Don't be as complex as magit 2 (magit 1 was really nice)

## Design

- s to stage a whole file
- u to unstage a file
- c to commit
- h to open a diffview, where you can stage or unstage individual chunks
 - s to stage a chunk
 - u to unstage a chunk
- z to stash (rest of details left to later)

## TODO:

- steal ideas and code from
 - gitstatus
 - treeview-git-status
 - git-diff-details
 - tualo-git-context
