#!/bin/bash
# All HIVE games in 'plays/all' is sorted into 3 categories:
#
# - dumbot      : All games against Dumbot
# - tournament  : All tournament games
# - player      : All games with 2 real players
#
# All plays are then sorted based on their type:
# - m (mosquito), l (ladybug), p (pillbug)
#
# Requires gawk installed: sudo apt-get install gawk
#
# v1: Initial version
#

PLAYS_DIR=../plays/all
DUMBOT_DIR=../plays/dumbot/
TOURNAMENT_DIR=../plays/tournament/
PLAYER_DIR=../plays/players/

# Phase 1: Coarse sort

#Make target dirs
mkdir -p $DUMBOT_DIR
mkdir -p $TOURNAMENT_DIR
mkdir -p $PLAYER_DIR

# Move all Dumbot games
find $PLAYS_DIR -name "*-Dumbot-*" -type f | xargs -I % mv % $DUMBOT_DIR

# Move all tournament games
find $PLAYS_DIR -name "T\!*" -type f | xargs -I % mv % $TOURNAMENT_DIR

# Move all player games (The rest)
find $PLAYS_DIR -name "*.sgf" -type f | xargs -I % mv % $PLAYER_DIR

# Phase 2: Sort game types into seperate directories
for dir in $DUMBOT_DIR $TOURNAMENT_DIR $PLAYER_DIR
do
    cd $dir
    find ./ -type f -name "*.sgf" | xargs -n1 -I % gawk 'BEGIN {OFS = ""}
          match($0, /^SU\[(.*)\]/, a) {
            print "mkdir -p ",a[1],"; mv ",FILENAME," ./"a[1],"/",FILENAME | "sh"
          }
         ' %
    cd ../../tools
done