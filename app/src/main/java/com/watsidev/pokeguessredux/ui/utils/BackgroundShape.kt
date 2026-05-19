package com.watsidev.pokeguessredux.ui.utils

import androidx.compose.foundation.shape.GenericShape

val BottomCurvedShape = GenericShape { size, _ ->
    // Define qué tanta altura de tu contenedor ocupará la curva (ej. el 25%)
    val curveDepth = size.height * 0.20f

    // 1. Iniciar en la esquina superior izquierda
    moveTo(0f, 0f)

    // 2. Dibujar línea hasta la esquina superior derecha
    lineTo(size.width, 0f)

    // 3. Dibujar línea hacia abajo en el lado derecho, deteniéndose antes del final
    lineTo(size.width, size.height - curveDepth)

    // 4. Dibujar la curva suave hacia el lado izquierdo
    quadraticBezierTo(
        x1 = size.width / 2f, y1 = size.height + (curveDepth * 0.5f), // Punto de control (tira de la curva hacia abajo)
        x2 = 0f, y2 = size.height - curveDepth // Punto final de la curva (borde izquierdo)
    )

    // 5. Cerrar la forma devolviéndola al inicio
    close()
}
