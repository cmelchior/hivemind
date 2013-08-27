#
# Plots the distribution of the different game types analyzed
#
# v1: Initial version
#
set title "10 most used openings when White wins"

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
plot '../results/white_winner_openings.data' every ::1::10 using (column(0)):3:xtic(1) with boxes, '' every ::1::10 using (column(0)):($3+0.25):(sprintf("%.2f%%", $3)) with labels