package com.nexus.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.app.R
import com.nexus.app.ui.theme.NexusAccent
import com.nexus.app.ui.theme.NexusBackground
import com.nexus.app.ui.theme.NexusEmerald
import com.nexus.app.ui.theme.NexusSurface
import com.nexus.app.ui.theme.NexusTextPrimary
import com.nexus.app.ui.theme.NexusTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SplashStage {
    LOGO,
    WELCOME
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var stage by remember { mutableStateOf(SplashStage.LOGO) }

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
        },
        label = "splashTransition"
    ) { currentStage ->
        when (currentStage) {
            SplashStage.LOGO -> {
                LogoSplashContent(
                    onLogoFinished = {
                        stage = SplashStage.WELCOME
                    }
                )
            }
            SplashStage.WELCOME -> {
                WelcomeContent(
                    onContinue = onSplashFinished
                )
            }
        }
    }
}

@Composable
fun LogoSplashContent(onLogoFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0.6f) }
    val glowAlpha = remember { Animatable(0f) }

    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(30f) }

    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        // Brillo radial
        launch {
            glowAlpha.animateTo(0.6f, animationSpec = tween(400))
            glowScale.animateTo(1.5f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            glowAlpha.animateTo(0f, animationSpec = tween(500))
        }

        // Transición logo
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(400))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Aparecer y deslizarse hacia arriba
        delay(300)
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            textOffsetY.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        }

        // 4. Transición a la pantalla de bienvenida
        delay(1600)
        onLogoFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF131D30),
                        NexusBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Brillante
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(glowScale.value)
                .alpha(glowAlpha.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NexusAccent.copy(alpha = 0.6f),
                            NexusEmerald.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nexus_logo),
                contentDescription = "Nexus Logo",
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value * pulseScale)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffsetY.value.dp)
                    .alpha(textAlpha.value)
            ) {
                Text(
                    text = "N E X U S",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Red de notas interconectadas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NexusTextSecondary,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun WelcomeContent(onContinue: () -> Unit) {
    Scaffold(
        containerColor = NexusBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "Organiza tus",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexusTextPrimary
                    )
                )
                Text(
                    text = "pensamientos",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = NexusAccent
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Escribe libremente y deja que NEXUS dibuje las conexiones entre tus ideas.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NexusTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                )
            }

            // Tarjeta central con gráfico giratorio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = NexusSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        RotatingGraphIllustration()
                    }
                }
            }

            // Boton Continuar
            Button(
                onClick = onContinue,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexusAccent,
                    contentColor = NexusBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = NexusBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NexusBackground
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RotatingGraphIllustration() {
    // Animación de rotación continua para los nodos del gráfico
    val infiniteTransition = rememberInfiniteTransition(label = "graphRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Animación para el tamaño y brillo del nodo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        rotate(degrees = rotationAngle, pivot = center) {
            // Posiciones de los nodos
            val n1 = Offset(w * 0.50f, h * 0.50f) // Nodo principal del centro
            val n2 = Offset(w * 0.35f, h * 0.25f) // Nodo superior izquierdo
            val n3 = Offset(w * 0.68f, h * 0.28f) // Nodo superior derecho
            val n4 = Offset(w * 0.82f, h * 0.36f) // Nodo superior derecho lejano
            val n5 = Offset(w * 0.58f, h * 0.80f) // Nodo inferior

            val accentColor = NexusAccent
            val lineColor = NexusEmerald.copy(alpha = 0.45f)
            val lineThick = 2.dp.toPx()

            // Líneas de conexión entre los nodos del gráfico
            val connections = listOf(
                n1 to n2, n1 to n3, n1 to n5,
                n2 to n3, n3 to n4, n3 to n5, n4 to n5
            )

            connections.forEach { (start, end) ->
                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = lineThick
                )
            }

            // Dibuja nodos brillante sutil
            val nodes = listOf(
                Triple(n1, 28.dp.toPx() * pulse, 0.28f),
                Triple(n2, 18.dp.toPx() * pulse, 0.22f),
                Triple(n3, 22.dp.toPx() * pulse, 0.22f),
                Triple(n4, 13.dp.toPx() * pulse, 0.18f),
                Triple(n5, 16.dp.toPx() * pulse, 0.22f)
            )

            nodes.forEach { (pos, radius, alpha) ->
                // Resplandor exterior
                drawCircle(
                    color = accentColor.copy(alpha = alpha),
                    radius = radius * 1.4f,
                    center = pos
                )
                // Nodo central
                drawCircle(
                    color = accentColor,
                    radius = radius,
                    center = pos
                )
            }
        }
    }
}
