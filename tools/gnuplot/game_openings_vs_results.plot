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
set grid ytics
set ytics 5
set yrange[0:80]
set format y "%.0f%%";
set key nobox inside top
set key font ",10"
set key spacing 1
set style data histogram
set style histogram cluster gap 2
set style fill solid border -1
set terminal png size 1800,600 enhanced font "Verdana,10"
set output output;

#Plot
plot for [COL=STARTCOL:ENDCOL] '../results/game_openings_vs_results_percentage.data' u COL:xtic(1) w histogram title columnheader(COL), \
     for [COL=STARTCOL:ENDCOL] '../results/game_openings_vs_results_percentage.data' u (column(0)-1+BOXWIDTH*(COL-STARTCOL+GAPSIZE/2+1)-0.5):COL:(sprintf("%.1f", column(COL))) notitle w labels center offset 0,0.5