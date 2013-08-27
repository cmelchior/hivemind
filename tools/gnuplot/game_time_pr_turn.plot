#
# Plots the distribution of the different game types analyzed
#
# v1: Initial version
#
set title "Game length pr. turn"

#Setup
clear
reset
set grid ytics
set nokey
set style data histogram
set style histogram rowstacked
set style fill solid border -1
set terminal png size 1200,400 enhanced font "Verdana,8"
set boxwidth 0.5
set output output;

#Plot
plot '../results/game_time_pr_turn.data' using 2:xtic(1) with boxes