#!/bin/bash
# Download all HIVE games from boardspace.net into /plays/downloads.
# All games are unzipped and put into /plays/all
#
# v1: Initial version
#

DOWNLOAD_DIR=../plays/downloads
PLAY_DIR=../plays/all

# Download all files
wget --recursive --no-parent --no-host-directories --accept=.zip,.sgf --directory-prefix=../plays/downloads --wait 1 http://www.boardspace.net/hive/hivegames

# Unzip all game find
archives $DOWNLOAD_DIR -name '*.zip' | xargs -n1 -I % unzip % -d $PLAY_DIR

# Copy remaining .sgf files
find $DOWNLOAD_DIR -name "*.sgf" -type f | xargs -n1 -I % cp % $PLAY_DIR

