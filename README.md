# Logic Maze

Logic Maze is a console-based Java game that combines maze navigation with Boolean expressions, expression trees, truth tables, and Karnaugh maps.

## Overview

The player explores a 21 x 45 maze, collects logic symbols, avoids robots, and builds a valid expression tree. After completing the tree, the game generates its infix and postfix forms, creates a truth table, and displays the related Karnaugh map.

## Features

- Random and target-based robot movement
- Backpack and expression-tree storage modes
- Collectible logic symbols and fireballs
- Infix and postfix expression generation
- Truth-table questions with score calculation
- Four-variable Karnaugh map
- High-score list stored with a doubly linked list

## Controls

### Maze screen

| Key | Action |
| --- | --- |
| Arrow keys or `W` `A` `S` `D` | Move the player |
| `Space` | Fire a collected fireball |
| `M` | Switch between Backpack and Tree storage |
| `1` | Open the maze screen |
| `2` | Open the tree screen |
| `3` | Open the table screen after completing a valid tree |

### Tree screen

| Key | Action |
| --- | --- |
| `W` | Move to the parent node |
| `A` | Move to the left child |
| `D` | Move to the right child |
| `T` | Place the top backpack symbol at the cursor |
| `R` | Return the cursor symbol to the backpack |
| `F` | Finish and validate the expression tree |

### Table screen

| Input | Action |
| --- | --- |
| `0` or `1` | Answer the missing truth-table value |
| Boolean expression + `Enter` | Submit the simplified expression |

## Requirements

- Java 21 or later
- Enigma console library, included in the `lib` folder

## Running the Project

1. Clone or download the repository.
2. Open the project folder in IntelliJ IDEA.
3. Select Java 21 as the project SDK.
4. Keep the project root as the working directory so `maze.txt` and `highscore.txt` can be found.
5. Run `src/Main.java`.

## Project Structure

```text
Logic-Maze/
|-- src/             Java source files
|-- lib/             Enigma console library
|-- maze.txt         Maze layout
`-- highscore.txt    Saved high scores
```
