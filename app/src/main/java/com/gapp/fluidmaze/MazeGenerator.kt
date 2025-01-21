package com.gapp.fluidmaze

class MazeGenerator(private val width: Int, private val height: Int) {
    private val maze = Array(height) { BooleanArray(width) { false } }
    private val directions = listOf(
        Pair(0, -1), Pair(0, 1),  // Up, Down
        Pair(-1, 0), Pair(1, 0)   // Left, Right
    )

    fun generate(): Array<BooleanArray> {
        carvePath(1, 1)
        maze[height - 2][width - 2] = true  // Single exit
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
