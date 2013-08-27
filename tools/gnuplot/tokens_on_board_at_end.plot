#
# Plots the distribution of the different game types analyzed
#
# v1: Initial version
#
set title "Tokens on board at game end - Diff"

#Setup
clear
reset
set grid ytics
set format y "%.0f%%";
set nokey
set style data histogram
set style histogram rowstacked
set style fill solid border -1
set terminal png size 1200,400 enhanced font "Verdana,9"
set boxwidth 0.5
set output output;

#Plot
plot '../results/bugs_on_board_at_end_diff.data' using 1:3:xtic(1) with boxes, '' using 1:($3+1.5):(sprintf("%.2f%%", $3)) with labels