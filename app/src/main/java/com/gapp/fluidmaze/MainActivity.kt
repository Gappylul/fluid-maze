package com.gapp.fluidmaze

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    var mazeSize by remember { mutableIntStateOf(15) }
    val cellSize = if (mazeSize > 20) 30f else 40f

    var mazeGenerator by remember { mutableStateOf(MazeGenerator(mazeSize, mazeSize)) }
    var fluidSimulator by remember { mutableStateOf(FluidSimulator(mazeSize, mazeSize)) }
    var maze by remember { mutableStateOf(mazeGenerator.generate()) }

    var playerX by remember { mutableIntStateOf(1) }
    var playerY by remember { mutableIntStateOf(1) }

    val exitX = mazeSize - 2
    val exitY = mazeSize - 2
    val context = LocalContext.current
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var level by remember { mutableIntStateOf(1) }

    // List of background colors for each level
    val backgroundColors = listOf(
        Color(0xFFEFEFEF), // Light Gray
        Color(0xFFB2EBF2), // Light Cyan
        Color(0xFFFFCCBC), // Light Orange
        Color(0xFFBBDEFB), // Light Blue
        Color(0xFFE1BEE7)  // Light Purple
    )

    val currentBackgroundColor = backgroundColors[level % backgroundColors.size]

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
        val completionTime = System.currentTimeMillis() - startTime
        Toast.makeText(context, "🎉 You won in ${completionTime / 1000} sec! Harder maze coming...", Toast.LENGTH_LONG).show()

        mazeSize += 2
        mazeGenerator = MazeGenerator(mazeSize, mazeSize)
        fluidSimulator = FluidSimulator(mazeSize, mazeSize)
        maze = mazeGenerator.generate()
        playerX = 1
        playerY = 1
        startTime = System.currentTimeMillis()

        level++
    }

    Column(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                if (abs(dragAmount.x) > abs(dragAmount.y)) {
                    if (dragAmount.x > 10 && playerX < mazeSize - 1 && maze[playerY][playerX + 1]) {
                        playerX++
                    } else if (dragAmount.x < -10 && playerX > 0 && maze[playerY][playerX - 1]) {
                        playerX--
                    }
                } else {
                    if (dragAmount.y > 10 && playerY < mazeSize - 1 && maze[playerY + 1][playerX]) {
                        playerY++
                    } else if (dragAmount.y < -10 && playerY > 0 && maze[playerY - 1][playerX]) {
                        playerY--
                    }
                }
                change.consume()
            }
        },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Level: $level",
            color = Color.Black,
            style = TextStyle(fontSize = 24.sp)
        )

        Box(modifier = Modifier.weight(1f).background(currentBackgroundColor)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = (size.width - (mazeSize * cellSize)) / 2
                val startY = (size.height - (mazeSize * cellSize) - 100) / 2

                for (y in 0 until mazeSize) {
                    for (x in 0 until mazeSize) {
                        val color = when {
                            x == exitX && y == exitY -> Color.Green
                            maze[y][x] -> Color.White
                            else -> Color.Black
                        }
                        drawRect(
                            color,
                            topLeft = Offset(startX + x * cellSize, startY + y * cellSize),
                            size = Size(cellSize, cellSize),
                            style = Fill
                        )
                    }
                }

                val fluidData = fluidSimulator.getFluidData()
                for (y in 0 until mazeSize) {
                    for (x in 0 until mazeSize) {
                        val intensity = (fluidData[y][x] * 255).coerceIn(0.0, 255.0).toInt()
                        drawRect(
                            Color(0, 0, 255, intensity),
                            topLeft = Offset(startX + x * cellSize, startY + y * cellSize),
                            size = Size(cellSize, cellSize),
                            style = Fill
                        )
                    }
                }

                drawRect(
                    Color.Red,
                    topLeft = Offset(startX + playerX * cellSize, startY + playerY * cellSize),
                    size = Size(cellSize, cellSize),
                    style = Fill
                )
            }
        }

        if (playerX == exitX && playerY == exitY) restartGame()
    }
}
