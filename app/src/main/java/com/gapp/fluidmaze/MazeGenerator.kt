package com.gapp.fluidmaze

class MazeGenerator(private val width: Int, private val height: Int) {
    private val maze = Array(height) { BooleanArray(width) { false } }
    private val directions = listOf(
        Pair(0, -1), // Up
        Pair(0, 1),  // Down
        Pair(-1, 0), // Left
        Pair(1, 0)   // Right
    )

    fun generate(): Array<BooleanArray> {
        carvePath(1, 1)
        maze[height - 2][width - 2] = true  // Create a single exit
        return maze
    }

    private fun carvePath(x: Int, y: Int) {
        maze[y][x] = true
        directions.shuffled().forEach { (dx, dy) ->
            val nx = x + dx * 2
            val ny = y + dy * 2
            if (nx in 1 until width - 1 && ny in 1 until height - 1 && !maze[ny][nx]) {
                maze[y + dy][x + dx] = true
                carvePath(nx, ny)
            }
        }
    }
}
