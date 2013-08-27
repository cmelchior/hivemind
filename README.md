HIVEMIND
=================

HiveMind is a Java based stand-alone AI implementation for the boardgame Hive by John Yianni.

HiveMind currently consists of 3 seperate modules:

- A model of the game, including all rules except for the Pillbug.

- A AI game controller that can pitch AI's against each other and output the results including several basic AI implementations.

- A Boardspace.net game parser and statistics tool. Boardspace.net makes it possible to play Hive for free online, and
  have the largest collection of game data for Hive (currently 140.000+ games).

HiveMind is intended to be really easy to integrate into a game engine and the interface only consists of 1 method.

Document is Work-in-Progres!

Current status
-----------------
Alpha:
    - All board game rules except Pillbug.
    - AI Game controller working. Statistics still need work.
    - Some basic AI implementations working: Standard Minimax variants, Standard Monte Carlo tree search + UTC.

TODO
-----------------
- Make plots of metrics.

- Make write-up of stats for Boardspace.net / Boardgame geek
- Good Minimax heuristic function
- Good Monte Carlo heuristic function
- Consider how transposition tables should work
- Method profilling (can game model be improved?)
- Better comparison of AI battles
- Add threading to AI battles.
- Find possible improvements to Minimax
- Find possible improvements to Monte Carlo Tree Search


AI's
-----------------
The current implementations focus on variants of Minimax and Monte Carlo Tree Search algorithms. For full information
games, which Hive is, these kinds of AI's are the most prominent.

An AI is always time restricted, so the first and most important parameter is determining how much time the AI has to
calculate its next move. This is a difficult trade-off if the AI is playing a real person as that person cannot do
anything while the AI thinks.

Minimax algorithms are restricted by their heuristic functions, while a Monte Carlo algorithm techincally can play
without one (altough it becomes significantly better by using one).

The basic algorithms are fairly simple to implement for both Minimax and Monte Carlo Tree Search, however a lot of
additions has been suggested for improving their performance. Constructing a good AI is thus mostly a question of incorporating
those additions that makes most sense for the domain as well as finetuning parameters.


MiniMax
-----------------
- Alpha Beta
- Iterative Deepening
- Killer heuristic
- Transpostion tables
- Good heuristic function
- Parallelism
- Negamax, MDT(F), others?
- Opening book

Monte Carlo Tree Search
-----------------
- UTC
- Transposition tables

Other AI methods
-----------------
- Reinforcement learning? Variants? (Reported very bad performance, but work could be questioned?)
- Genetic algorithms? Variants (Usually used for optimization problems)


Boardspace.net Statistics
-----------------
22000+ games has been analyzed in order to make educated guesses for AI parameters.
All data can be found in the /result folder.

The following metrics have been looked at:

+ GamesAnalysedMetric: How many games have been analyzed in each category?
+ GameDurationTurnsMetric: How many turns does it take to win?
+ GameResultsPrColorMetric: How did the games turn out pr. color / game type.
+ GameDurationTimeMetric: What is the average time pr. turn
+ OpeningTokenMetric: What token is the first placed?
+ LastTokenMetric: What token is the last to be placed, ending the game?
+ BugsInSupplyAtGameEnd: How many tokens are not in play when the game is over.
+ BugsOnBoardAtGameEnd: How many tokens on the board when the game is over.
+ WinnerOpeningMetric: The first 4 moves by the winner.
+ FreeTokensPrTurnMetric: How many free tokens pr. turn for winner/looser.
 TokensAroundQueenMetric: How many tokens are around the queen pr. turn for winner/looser.

The results are also discusses here: http://www.boardgamegeek.com/thread/1028923/boardspace-net-hive-statistics

Links
-----------------
Hive: http://www.hivemania.com/
Boardspace.net: http://boardspace.net/english/about_hive.html



