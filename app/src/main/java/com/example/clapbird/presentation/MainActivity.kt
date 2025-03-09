package com.example.clapbird.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.clapbird.presentation.theme.ClapBirdTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            ClapBirdTheme {
                FlappyBirdGame()
            }
        }
    }
}
@Preview
@Composable
fun FlappyBirdGame() {
    // Game state
    var gameState by remember { mutableStateOf(GameState.READY) }
    var birdY by remember { mutableStateOf(0f) }
    var birdVelocity by remember { mutableStateOf(0f) }
    var score by remember { mutableStateOf(0) }
    var pipes by remember { mutableStateOf(emptyList<Pipe>()) }
    val coroutineScope = rememberCoroutineScope()

    // Constants
    val gravity = 0.5f
    val jumpVelocity = -10f
    val birdSize = 30f
    val pipeWidth = 60f
    val gapHeight = 150f
    val canvasHeight = 300f
    val canvasWidth = 300f

    // Initialize game
    fun resetGame() {
        birdY = canvasHeight / 2
        birdVelocity = 0f
        score = 0
        pipes = listOf(
            createPipe(canvasWidth + 100, canvasHeight, pipeWidth, gapHeight),
            createPipe(canvasWidth + 300, canvasHeight, pipeWidth, gapHeight)
        )
        gameState = GameState.READY
    }

    // Game loop
    fun startGameLoop() {
        coroutineScope.launch {
            gameState = GameState.PLAYING
            while (gameState == GameState.PLAYING) {
                // Update bird position
                birdVelocity += gravity
                birdY += birdVelocity

                // Update pipes
                pipes = pipes.map { pipe ->
                    pipe.copy(x = pipe.x - 2f)
                }.toMutableList()

                // Add new pipe when needed
                if (pipes.isNotEmpty() && pipes.last().x < canvasWidth - 200) {
                    pipes = pipes + createPipe(canvasWidth + 50, canvasHeight, pipeWidth, gapHeight)
                }

                // Remove pipes that are off-screen
                pipes = pipes.filter { it.x + pipeWidth > 0 }

                // Check for collisions
                val birdRect = BirdRect(
                    left = canvasWidth / 3 - birdSize / 2,
                    top = birdY - birdSize / 2,
                    right = canvasWidth / 3 + birdSize / 2,
                    bottom = birdY + birdSize / 2
                )

                // Collision with floor or ceiling
                if (birdY < 0 || birdY > canvasHeight) {
                    gameState = GameState.GAME_OVER
                }

                // Collision with pipes
                for (pipe in pipes) {
                    if (isCollision(birdRect, pipe, pipeWidth)) {
                        gameState = GameState.GAME_OVER
                    }

                    // Update score when bird passes pipe
                    if (!pipe.passed && pipe.x + pipeWidth < canvasWidth / 3 - birdSize / 2) {
                        pipe.passed = true
                        score++
                    }
                }

                delay(16) // ~60 FPS
            }
        }
    }

    // Reset game on first composition
    LaunchedEffect(Unit) {
        resetGame()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable {
                when (gameState) {
                    GameState.READY -> startGameLoop()
                    GameState.PLAYING -> birdVelocity = jumpVelocity
                    GameState.GAME_OVER -> resetGame()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGameBackground()

            // Draw bird
            drawBird(canvasWidth / 3, birdY, birdSize)

            // Draw pipes
            for (pipe in pipes) {
                drawPipe(pipe, pipeWidth)
            }
        }

        // UI Overlays
        when (gameState) {
            GameState.READY -> {
                Text(
                    text = "Tap to Start",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 50.dp)
                )
            }
            GameState.PLAYING -> {
                Text(
                    text = score.toString(),
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(top = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
            GameState.GAME_OVER -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Game Over\nScore: $score\nTap to Restart",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Draw game background
private fun DrawScope.drawGameBackground() {
    // Sky
    drawRect(color = Color(0xFF87CEEB))

    // Ground
    drawRect(
        color = Color(0xFF964B00),
        topLeft = Offset(0f, size.height - 30),
        size = Size(size.width, 30f)
    )
}

// Draw the bird
private fun DrawScope.drawBird(x: Float, y: Float, size: Float) {
    drawCircle(
        color = Color.Yellow,
        radius = size / 2,
        center = Offset(x, y)
    )

    // Add eye
    drawCircle(
        color = Color.Black,
        radius = size / 10,
        center = Offset(x + size / 4, y - size / 6)
    )

    // Add beak
    drawRect(
        color = Color(0xFFFF6600),
        topLeft = Offset(x + size / 3, y),
        size = Size(size / 3, size / 6)
    )
}

// Draw a pipe
private fun DrawScope.drawPipe(pipe: Pipe, pipeWidth: Float) {
    // Top pipe
    drawRect(
        color = Color.Green,
        topLeft = Offset(pipe.x, 0f),
        size = Size(pipeWidth, pipe.gapY)
    )

    // Bottom pipe
    drawRect(
        color = Color.Green,
        topLeft = Offset(pipe.x, pipe.gapY + pipe.gapHeight),
        size = Size(pipeWidth, size.height - pipe.gapY - pipe.gapHeight)
    )
}

// Create a new pipe
fun createPipe(x: Float, canvasHeight: Float, pipeWidth: Float, gapHeight: Float): Pipe {
    val minGapY = 50f
    val maxGapY = canvasHeight - gapHeight - 50f
    val gapY = Random.nextFloat() * (maxGapY - minGapY) + minGapY

    return Pipe(x, gapY, gapHeight, false)
}

// Check for collision between bird and pipe
fun isCollision(birdRect: BirdRect, pipe: Pipe, pipeWidth: Float): Boolean {
    // Check if bird is within pipe's x-range
    if (birdRect.right < pipe.x || birdRect.left > pipe.x + pipeWidth) {
        return false
    }

    // Check if bird is within the gap
    return birdRect.top < pipe.gapY || birdRect.bottom > pipe.gapY + pipe.gapHeight
}

// Game state
enum class GameState {
    READY, PLAYING, GAME_OVER
}

// Pipe data class
data class Pipe(
    val x: Float,
    val gapY: Float,
    val gapHeight: Float,
    var passed: Boolean
)

// Simple rectangle for collision detection
data class BirdRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)