#
# Plots the distribution of the different game types analyzed
#
# v1: Initial version
#
set title "First token on the board - Weighted by pieces available"

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
plot '../results/weighted_opening_token.data' every ::0::7 using 1:3:xtic(2) with boxes, '' every ::0::7 using 1:($3+0.75):(sprintf("%.2f%%", $3)) with labels