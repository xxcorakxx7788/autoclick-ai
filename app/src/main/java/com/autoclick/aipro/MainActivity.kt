
package com.autoclick.aipro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40,60,40,40)
        }

        val title = TextView(this).apply { text = "🤖 AutoClick AI Pro"; textSize = 24f }
        val objetivoInput = EditText(this).apply {
            hint = "Ej: dale click a COMPRAR cuando aparezca, si aparece anuncio cierralo"
            setText(AIBrain.objetivo)
        }
        val modoSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("MIXTO - IA Total","LECTURA - Solo Texto","VISUAL - Botones"))
        }
        val btnPermiso = Button(this).apply { text = "1. Activar Accesibilidad" }
        val btnIniciar = Button(this).apply { text = "▶ INICIAR IA" }
        val btnDetener = Button(this).apply { text = "⏹ DETENER" }
        val status = TextView(this).apply { text = "Estado: Detenido" }

        btnPermiso.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        btnIniciar.setOnClickListener {
            AIBrain.objetivo = objetivoInput.text.toString()
            AutoClickService.instance?.iniciarLoop()
            status.text = "Estado: ✅ IA Activa buscando: ${AIBrain.objetivo}"
            Toast.makeText(this, "IA iniciada. Minimiza la app y abre la otra app.", Toast.LENGTH_LONG).show()
        }
        btnDetener.setOnClickListener {
            AutoClickService.instance?.detener()
            AIBrain.isRunning = false
            status.text = "Estado: Detenido"
        }

        layout.addView(title)
        layout.addView(TextView(this).apply { text = "\nObjetivo en lenguaje natural:" })
        layout.addView(objetivoInput)
        layout.addView(TextView(this).apply { text = "\nModo IA:" })
        layout.addView(modoSpinner)
        layout.addView(btnPermiso)
        layout.addView(btnIniciar)
        layout.addView(btnDetener)
        layout.addView(status)
        layout.addView(TextView(this).apply { text = "\n\nCómo usar:\n1. Activa accesibilidad\n2. Escribe qué quieres que clickee\n3. Dale INICIAR\n4. Minimiza y abre la app donde quieras autoclick\n5. La IA leerá sola y clickeará" })

        setContentView(layout)
    }
}
