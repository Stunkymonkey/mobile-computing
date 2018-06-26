#!/bin/bash

tmux new-session -d -s "mc"
tmux split-window -h
tmux split-window -h
tmux split-window -h
tmux split-window -h
tmux select-layout even-horizontal

tmux select-pane -t 1
tmux send-keys -t "mc" "ssh mobile-computing-4" C-m
tmux select-pane -t 2
tmux send-keys -t "mc" "ssh mobile-computing-3" C-m
tmux select-pane -t 3
tmux send-keys -t "mc" "ssh mobile-computing-5" C-m
tmux select-pane -t 4
tmux send-keys -t "mc" "ssh mobile-computing-1" C-m
tmux select-pane -t 5
tmux send-keys -t "mc" "ssh mobile-computing-2" C-m

tmux set-window-option -t "mc" "synchronize-panes"

tmux attach -t "mc"