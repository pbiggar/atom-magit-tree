# atom-treecommit

Make git operations first class editor functions.

Based loosely on Emacs' amazing Magit.

## Goals

- Keep your fingers on the keyboard
- Be able to stage chunks easily
- Be able to create precise commits quickly
- Allow most simple operations from the editor
- Don't be as complex as magit 2 (magit 1 was really nice)

## What you can do (planned)

Shows status:
- shows git cli colors and status in the gutter, and in tree-view

Control commits from the editor:
- `ctrl-cmd-S`: stage file
- `ctrl-cmd-U`: unstage file
- `ctrl-cmd-s`: stage hunk
- `ctrl-cmd-[`: stage hunk and go to next hunk
- `ctrl-cmd-]`: stage hunk and go to prev hunk
- `ctrl-cmd-u`: unstage hunk
- `ctrl-cmd-1`: show normal treeview
- `ctrl-cmd-2`: show only changed files
- `ctrl-cmd-3`: show changed files and their diffs
- `ctrl-cmd-n`: open next changed thing in tree-view
- `ctrl-cmd-p`: open prev changed thing in tree-view


## TODO (maybe)
- commit
- push
- pull
- stash
- or maybe leave that all to other packages
