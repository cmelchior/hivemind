#
# Plot how many turns it take to end the game with win/draw.
#
# v1: Initial version
#
set title "Game turns"

# Variabels
# See http://stackoverflow.com/questions/12926827/gnuplot-how-to-place-y-values-above-bars-when-using-histogram-style
if (!exists("columns")) print "'columns' must eb provided as a parameter'"; exit

#Setup
clear
reset
set xtics 5
set grid ytics
set key box inside top
set terminal pngcairo enhanced font "arial,10" fontscale 1.0 size 1800,600; set zeroaxis;;
set output output
;
#Plot
plot for [COL=2:columns] '../results/game_turns.data' using COL:xticlabels(1) with lines lw 1 title columnheader
