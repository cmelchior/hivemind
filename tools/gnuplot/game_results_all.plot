#
# Plots the games results for the game types: All, Player, Tournament, Dumbot
#
# v1: Initial version
#
set title "Game result"

# Variabels
# See http://stackoverflow.com/questions/12926827/gnuplot-how-to-place-y-values-above-bars-when-using-histogram-style
if (!exists("columns")) print "'columns' must be provided as a parameter'"; exit
GAPSIZE=2
STARTCOL=2      #Start plotting data in this column (2 for your example)
ENDCOL=columns  #Last column of data to plot (10 for your example)
NCOL=ENDCOL-STARTCOL+1     #Number of columns we're plotting
BOXWIDTH=1./(GAPSIZE+NCOL) #Width of each box.

#Setup
clear
reset
#set xtics rotate out
#set noxtics
set grid ytics
set yrange[0:100]
set ytics 10
set format y "%.0f%%";
set key box inside top
#set nokey
set style data histogram
set style histogram cluster gap 2
#set style histogram clustered
set style fill solid border -1
set terminal png size 1800,600 enhanced font "Helvetica,14"
#set boxwidth 0.9
set output output;

#Plot
plot for [COL=2:columns] '../results/game_results.data' using COL:xticlabels(1) title columnheader
