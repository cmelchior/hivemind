#!/bin/bash
# Generate plots for metrics extracted from game files.
#
# v1: Initial version
#

# Games analyzed
STARTROW=$(($(grep "all" ../results/games_analyzed.data | wc -l) + 1))
echo $STARTROW
eval "gnuplot -e \"startrow=$STARTROW;output='../results/games_analyzed.png'\" ./gnuplot/games_analyzed.plot"

# Game result plot - All
COLUMNS=$(($(head -1 ../results/game_results.data | tr -d "\n" | tr "\t" "\n" | wc -l) + 1))
eval "gnuplot -e \"columns=$COLUMNS;output='../results/game_results_all.png'\" ./gnuplot/game_results_all.plot"

# Game result plot - Only types of games
COLUMNS=4
eval "gnuplot -e \"columns=$COLUMNS;output='../results/game_results_types.png'\" ./gnuplot/game_results_types.plot"

# Number of turns - All
COLUMNS=$(head -1 ../results/game_turns.data | tr -d "\n" | tr "\t" "\n" | wc -l)+1
eval "gnuplot -e \"columns=$COLUMNS;output='../results/game_turns_all.png'\" ./gnuplot/game_turns.plot"

# Number of turns - Types of games
COLUMNS=4
eval "gnuplot -e \"columns=$COLUMNS;output='../results/game_turns_types.png'\" ./gnuplot/game_turns.plot"

# Time pr. game
eval "gnuplot -e \"output='../results/game_time.png'\" ./gnuplot/game_time.plot"

# Time pr. turn
eval "gnuplot -e \"output='../results/game_time_pr_turn.png'\" ./gnuplot/game_time_pr_turn.plot"

# Weighted opening token
eval "gnuplot -e \"output='../results/opening_token.png'\" ./gnuplot/first_token.plot"

# Weighted closing token
eval "gnuplot -e \"output='../results/closing_token.png'\" ./gnuplot/last_token.plot"

# Token diff at game end
eval "gnuplot -e \"output='../results/tokens_on_board_at_end.png'\" ./gnuplot/tokens_on_board_at_end.plot"

# White openings
eval "gnuplot -e \"output='../results/white_openings.png'\" ./gnuplot/white_openings.plot"

# Black openings
eval "gnuplot -e \"output='../results/black_openings.png'\" ./gnuplot/black_openings.plot"

# Black winner openings
eval "gnuplot -e \"output='../results/black_winner_openings.png'\" ./gnuplot/black_winner_openings.plot"

# White winner openings
eval "gnuplot -e \"output='../results/white_winner_openings.png'\" ./gnuplot/white_winner_openings.plot"
