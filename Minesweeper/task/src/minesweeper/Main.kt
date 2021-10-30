package minesweeper

import kotlin.random.Random

fun main() {
    val mines = requestMines()
    val minefield = Minefield(mines)

    do {
        minefield.printField()
        minefield.playerInteraction()
        minefield.completionCheck()
    } while (minefield.playerState == PlayerState.PLAYING)

    if (minefield.playerState == PlayerState.WON) println("Congratulations! You found all the mines!")
    else if (minefield.playerState == PlayerState.LOST) println("You stepped on a mine and failed!")
}

fun requestMines(): Int {
    println("How many mines do you want on the field?")
    var mines: Int
    do {
        mines = readLine()!!.toInt()
    } while (mines > 81 || mines < 1)
    return mines
}

enum class PlayerState {
    WON,
    LOST,
    PLAYING
}

class Minefield {

    private var mineCounter: Int
    private val minefield = MutableList(9) { MutableList(9) { '.' } }
    //private val playerMarks = MutableList(9) { MutableList(9) { '.' } }
    private val playerViewMinefield = MutableList(9) { MutableList(9) { '.' } }
    var playerState = PlayerState.PLAYING
    var firstInteraction = true

    constructor(_mines: Int) {
        mineCounter = _mines
        placeMines()
    }

    fun printField() {

        println(" |123456789|")
        println("—|—————————|")
        for (i in 0..8) {
            println("${i + 1}|${playerViewMinefield[i].joinToString("")}|")
        }
        println("—|—————————|")
    }

    fun playerInteraction() {
        println("Set/unset mine marks or claim a cell as free:")
        val input = readLine()!!.toString()
        val action = input.substring(4)
        val coordinates = input.split(" ")
        val j = coordinates[0].toInt() - 1
        val i = coordinates[1].toInt() - 1

        when (action) {
            "mine" -> {
                when {
                    playerViewMinefield[i][j] == '.' -> {
                        playerViewMinefield[i][j] = '*'
                    }
                    playerViewMinefield[i][j] == '*' -> {
                        playerViewMinefield[i][j] = '.'
                    }
                    else -> {
                        println("There is a number here!")
                        playerInteraction()
                    }
                }
            }
            "free" -> {
                when {
                    playerViewMinefield[i][j] == '/' -> {
                        firstInteraction = false
                        playerInteraction()
                    }
                    minefield[i][j] == '.' -> {
                        firstInteraction = false
                        updatePlayerView(i, j)
                    }
                    minefield[i][j].isDigit() -> {
                        firstInteraction = false
                        updatePlayerView(i, j)
                    }
                    minefield[i][j] == 'X' -> {
                        if (!firstInteraction) {
                            playerState = PlayerState.LOST
                        } else {
                            do {
                                clearField()
                                placeMines()
                            } while (minefield[i][j] == 'X')
                            firstInteraction = false
                            updatePlayerView(i, j)
                        }
                    }
                }
            }
            else -> playerInteraction()
        }
    }

    private fun updatePlayerView(i: Int, j: Int) {
        when {
            playerViewMinefield[i][j] == '/' -> Unit
            minefield[i][j] == '.' -> {
                playerViewMinefield[i][j] = '/'
                for (k in -1..1) {
                    for (l in -1..1) {
                        val rangeY = (i + k).coerceIn(0, 8)
                        val rangeX = (j + l).coerceIn(0, 8)
                        updatePlayerView(rangeY, rangeX)
                    }
                }
            }
            minefield[i][j].isDigit() -> {
                playerViewMinefield[i][j] = minefield[i][j]
            }
            minefield[i][j] == 'X' -> Unit
        }
    }

    fun completionCheck() {
        for (i in 0..8) {
            for (j in 0..8) {
                if ((minefield[i][j] == 'X'  && playerViewMinefield[i][j] == '.') ||
                    (minefield[i][j] == '.'  && playerViewMinefield[i][j] == '*'))
                    return
            }
        }
        playerState = PlayerState.WON
    }

    private fun clearField() {
        for (i in 0..8) {
            for (j in 0..8) {
                minefield[i][j] = '.'
            }
        }
    }

    private fun placeMines() {
        val generator = Random
        val minePositions = mutableListOf<Int>()

        repeat(mineCounter) {
            var generationResult: Int
            do {
                generationResult = generator.nextInt(0, 81)
            } while (minePositions.contains(generationResult))
            minePositions += generationResult
        }

        for (i in minePositions.indices) {
            val row = minePositions[i] / 9
            val column = minePositions[i] % 9
            minefield[row][column] = 'X'
            placeMineMarkers(row, column)
        }
    }

    private fun placeMineMarkers(row: Int, column: Int) {
        for (i in row - 1..row + 1) {
            for (j in column - 1..column + 1) {
                when {
                    i < 0 || j < 0 -> Unit
                    i > 8 || j > 8 -> Unit
                    minefield[i][j] == '.' -> minefield[i][j] = '1'
                    minefield[i][j].isDigit() -> minefield[i][j] =
                        (minefield[i][j].digitToInt() + 1).digitToChar()
                }
            }
        }
    }
}