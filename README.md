HIVEMIND v.0.1
=================

Document is Work-in-Progres!

HiveMind is a Java based stand-alone AI implementation for the board game Hive by John Yianni.

HiveMind currently consists of 3 seperate modules:

- A model of the game, including all rules except for the Pillbug (comming soon).

- A AI game controller that can pitch AI's against each other and output the results including several AI implementations.

- A Boardspace.net game parser and statistics tool. Boardspace.net makes it possible to play Hive for free online, and
  have the largest collection of game data for Hive (currently 140.000+ games).


Current status
-----------------
Alpha:

- All board game rules are implemented except Pillbug.
- AI Game controller working. Statistics still need work.
- Some basic AI implementations working: Standard Minimax variants, Standard Monte Carlo tree search + UTC.


AI's
-----------------
The current implementations focus on variants of Minimax and Monte Carlo Tree Search algorithms. For perfect information
games, which Hive is, these kinds of AI's are the most prominent.

An AI is always time restricted, so the first and most important parameter is determining how much time the AI has to
calculate its next move. This is a difficult trade-off when the AI is playing a real person as that person cannot do
anything while the AI thinks (ie. boredom).

Minimax algorithms are restricted by their heuristic functions, while a Monte Carlo algorithm technically can play
without one (although it becomes significantly better by using one).

The basic algorithms are fairly simple to implement for both Minimax and Monte Carlo Tree Search, however a lot of
additions has been suggested for improving their performance. Constructing a good AI is thus mostly a question of
incorporating those additions that makes most sense for the domain as well as fine tuning parameters.


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
- Reinforcement learning? Variants? (Reported very bad performance, but work wasn't that thorough?)
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
+ TokensAroundQueenMetric: How many tokens are around the queen pr. turn for winner/looser.
+ CompareOpenings: Compare the most common openings and their success rates.

The results are also discusses here: http://www.boardgamegeek.com/thread/1028923/boardspace-net-hive-statistics


Running Boardspace.net Statistics
-----------------
First download all game files from boardspace.net:

    $ ./tools > sh download_hivegames.sh

Then run the sorting to extract games and put them in the correct folders:

    $ ./tools > bash sort_hivegames.sh

Then modify *MainParseGames.java* for your needs. No Jar + commandline interface currently exists, so you have to
edit the java file manually. When done, run the following command:

    $ > gradle run

All results are saved in the */results* folder. To generate graphs after the analysis. Run the following:

    $ ./tools > sh generate_plots.sh



TODO
-----------------
- (✓) Make plots of metrics.
- (✓) Make write-up of stats for Boardspace.net / Boardgamegeek

- Good Minimax heuristic function
- Good Monte Carlo heuristic function
- Consider how transposition tables should work
- Method profilling (can game model be improved?)
- Better comparison of AI battles
- Add threading to AI battles.
- Find possible improvements to Minimax
- Find possible improvements to Monte Carlo Tree Search


Links
-----------------
Hive: http://www.hivemania.com/

Boardspace.net: http://boardspace.net/english/about_hive.html


License
----------------
    Copyright 2013 Christian Melchior

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


