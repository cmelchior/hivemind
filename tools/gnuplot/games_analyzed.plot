#
# Plots the distribution of the different game types analyzed
#
# v1: Initial version
#
set title "Games analyzed"

#Variabels
if (!exists("startrow")) print "'startrow' must be provided as a parameter'"; exit

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
plot '../results/games_analyzed.data' every ::startrow using 1:4:xtic(2) with boxes, '' every ::startrow using 1:($4+2):3 with labels