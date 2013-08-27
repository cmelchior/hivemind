#
# Plot how many turns it take to end the game with win/draw.
#
# v1: Initial version
#
set title "Game turns"

# Variabels
# See http://stackoverflow.com/questions/12926827/gnuplot-how-to-place-y-values-above-bars-when-using-histogram-style
if (!exists("columns")) print "'columns' must be provided as a parameter'"; exit

#Setup
set grid ytics
set xtics 2
set xrange[0:100]
set key box inside top
set terminal pngcairo enhanced font "arial,10" fontscale 1.0 size 1800,600; set zeroaxis;;
set output output;

#Plot
plot for [COL=2:columns] '../results/game_turns.data' using 1:COL with lines lw 1 title columnheader
