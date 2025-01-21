package com.gapp.fluidmaze

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MazeGameScreen()
        }
    }
}

@Composable
fun MazeGameScreen() {
    var mazeSize by remember { mutableIntStateOf(25) } // base : 15
    val cellSize = 40f

    var mazeGenerator by remember { mutableStateOf(MazeGenerator(mazeSize, mazeSize)) }
    var fluidSimulator by remember { mutableStateOf(FluidSimulator(mazeSize, mazeSize)) }
    var maze by remember { mutableStateOf(mazeGenerator.generate()) }

    var playerX by remember { mutableIntStateOf(1) }
    var playerY by remember { mutableIntStateOf(1) }

    val exitX = mazeSize - 2
    val exitY = mazeSize - 2
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                fluidSimulator.step()
                delay(50)
            }
        }
    }

    fun restartGame() {
        mazeSize += 2  // Increase difficulty
        mazeGenerator = MazeGenerator(mazeSize, mazeSize)
        fluidSimulator = FluidSimulator(mazeSize, mazeSize)
        maze = mazeGenerator.generate()
        playerX = 1
        playerY = 1
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // Centered Maze
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = (size.width - (mazeSize * cellSize)) / 2
                val startY = (size.height - (mazeSize * cellSize) - 100) / 2

                // Draw maze
                for (y in 0 until mazeSize) {
                    for (x in 0 until mazeSize) {
                        val color = when {
                            x == exitX && y == exitY -> Color.Green // Mark exit in Green
                            maze[y][x] -> Color.White
                            else -> Color.Black
                        }
                        drawRect(
                            color,
                            topLeft = androidx.compose.ui.geometry.Offset(startX + x * cellSize, startY + y * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                            style = Fill
                        )
                    }
                }

                // Draw fluid
                val fluidData = fluidSimulator.getFluidData()
                for (y in 0 until mazeSize) {
                    for (x in 0 until mazeSize) {
                        val intensity = (fluidData[y][x] * 255).coerceIn(0.0, 255.0).toInt()
                        drawRect(
                            Color(0, 0, 255, intensity),
                            topLeft = androidx.compose.ui.geometry.Offset(startX + x * cellSize, startY + y * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                            style = Fill
                        )
                    }
                }

                // Draw player
                drawRect(
                    Color.Red,
                    topLeft = androidx.compose.ui.geometry.Offset(startX + playerX * cellSize, startY + playerY * cellSize),
                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                    style = Fill
                )
            }
        }

        // Touch Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { if (playerY > 0 && maze[playerY - 1][playerX]) playerY-- }) {
                Text("⬆")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { if (playerX > 0 && maze[playerY][playerX - 1]) playerX-- }) {
                Text("⬅")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if (playerX < mazeSize - 1 && maze[playerY][playerX + 1]) playerX++ }) {
                Text("➡")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { if (playerY < mazeSize - 1 && maze[playerY + 1][playerX]) playerY++ }) {
                Text("⬇")
            }
        }

        // Check for victory
        if (playerX == exitX && playerY == exitY) {
            LaunchedEffect(Unit) {
                Toast.makeText(context, "🎉 You won! Generating a harder maze...", Toast.LENGTH_LONG).show()
                restartGame()
            }
        }
    }
}
