
package com.autoclick.aipro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.random.Random

object AIBrain {
    var objetivo: String = "COMPRAR"
    var modo: String = "MIXTO" // LECTURA, VISUAL, MIXTO
    var delayMin = 300L
    var delayMax = 1200L
    var antiDeteccion = true
    var esperando = true
    var isRunning = false

    fun extraerKeywords(): List<String> {
        return objetivo.uppercase()
            .split(" ", ",", ".")
            .filter { it.length > 2 }
            .distinct()
    }
    
    fun debeClickearTexto(texto: String?): Boolean {
        if (texto.isNullOrBlank()) return false
        val keywords = extraerKeywords()
        val t = texto.uppercase()
        // IA simple: si contiene palabra clave o sinónimos comunes
        val sinonimos = mapOf(
            "COMPRAR" to listOf("BUY","ADD","CART","COMPRAR"),
            "CERRAR" to listOf("X","CLOSE","CERRAR","SKIP","OMITIR"),
            "LIKE" to listOf("ME GUSTA","LIKE","CORAZON","❤"),
            "SIGUIENTE" to listOf("NEXT","SIGUIENTE","CONTINUAR","CONTINUE")
        )
        for (k in keywords) {
            if (t.contains(k)) return true
            sinonimos[k]?.let { lista -> if (lista.any { t.contains(it) }) return true }
        }
        return false
    }
}

class AutoClickService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var loopRunnable: Runnable? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!AIBrain.isRunning) return
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            buscarYClickearInteligente()
        }
    }

    override fun onInterrupt() {}

    fun iniciarLoop() {
        AIBrain.isRunning = true
        loopRunnable = object : Runnable {
            override fun run() {
                if (!AIBrain.isRunning) return
                buscarYClickearInteligente()
                val delay = if (AIBrain.esperando) 800L else Random.nextLong(AIBrain.delayMin, AIBrain.delayMax)
                handler.postDelayed(this, delay)
            }
        }
        handler.post(loopRunnable!!)
    }

    fun detener() {
        AIBrain.isRunning = false
        loopRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun buscarYClickearInteligente() {
        val root = rootInActiveWindow ?: return
        val candidatos = mutableListOf<AccessibilityNodeInfo>()
        recolectarNodos(root, candidatos)

        // Prioridad 1: Anuncios / Cerrar
        val cerrar = candidatos.firstOrNull { 
            it.text?.toString()?.uppercase() in listOf("X","CERRAR","CLOSE","SKIP") ||
            it.contentDescription?.toString()?.uppercase()?.contains("CLOSE") == true
        }
        if (cerrar != null && AIBrain.objetivo.uppercase().contains("ANUNCIO")) {
            clickearNodo(cerrar)
            return
        }

        // Prioridad 2: Objetivo principal con IA
        val objetivoNodo = candidatos
            .filter { node -> AIBrain.debeClickearTexto(node.text?.toString() ?: node.contentDescription?.toString()) }
            .maxByOrNull { it.text?.length ?: 0 }

        if (objetivoNodo != null) {
            clickearNodo(objetivoNodo)
        } else if (!AIBrain.esperando) {
            // Si no esta en modo esperar, click en centro si es modo visual
            val bounds = android.graphics.Rect()
            root.getBoundsInScreen(bounds)
            clickearEn(bounds.centerX() + Random.nextInt(-15,15), bounds.centerY() + Random.nextInt(-15,15))
        }
    }

    private fun recolectarNodos(node: AccessibilityNodeInfo, lista: MutableList<AccessibilityNodeInfo>) {
        lista.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { recolectarNodos(it, lista) }
        }
    }

    private fun clickearNodo(node: AccessibilityNodeInfo) {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        clickearEn(rect.centerX(), rect.centerY())
    }

    private fun clickearEn(x: Int, y: Int) {
        val finalX = if (AIBrain.antiDeteccion) x + Random.nextInt(-15,15) else x
        val finalY = if (AIBrain.antiDeteccion) y + Random.nextInt(-15,15) else y
        
        val path = Path()
        path.moveTo(finalX.toFloat(), finalY.toFloat())
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()
        dispatchGesture(gesture, null, null)
    }

    companion object {
        var instance: AutoClickService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }
}
