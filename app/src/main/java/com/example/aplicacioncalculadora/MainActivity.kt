package com.example.aplicacioncalculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var tvRes: TextView? = null // Declaración de una variable para la TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRes = findViewById(R.id.tvRes) // Obtiene la referencia a la TextView en el diseño XML
    }
    fun calcular(view: View) {
        var boton = view as Button // Convierte la vista en un botón
        var textoBoton = boton.text.toString() // Obtiene el texto del botón
        var concatenar = tvRes?.text.toString() + textoBoton // Concatena el texto del botón a la TextView
        var concatenarSinCeros = quitarCerosIzquierda(concatenar) // Elimina ceros a la izquierda

        if (textoBoton == "=") { // Si se presiona el botón igual (=)
            var resultado = 0.0
            try {
                resultado = eval(tvRes?.text.toString()) // Evalúa la expresión matemática
                tvRes?.text = resultado.toString() // Muestra el resultado en la TextView
            } catch (e: Exception) {
                tvRes?.text = e.toString() // Muestra un mensaje de error en caso de excepción
            }
        } else if (textoBoton == "RESET") { // Si se presiona el botón RESET
            tvRes?.text = "0" // Restablece la TextView a "0"
        } else {
            tvRes?.text = concatenarSinCeros // Actualiza el contenido de la TextView
        }
    }
    fun quitarCerosIzquierda(str: String): String {
        var i = 0
        while (i < str.length && str[i] == '0') i++
        val sb = StringBuffer(str)
        sb.replace(0, i, "") // Elimina los ceros a la izquierda de la cadena
        return sb.toString() // Devuelve la cadena resultante sin ceros a la izquierda
    }

    fun eval(str: String): Double {
        // Se crea una instancia anónima de la clase Any para definir funciones internas.
        return object : Any() {
            var pos = -1
            var ch = 0

            // Función para avanzar al siguiente carácter en la cadena.
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            // Función para consumir un carácter específico y avanzar al siguiente, si coincide.
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar() // Saltar espacios en blanco.
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            // Función principal que inicia el análisis de la expresión.
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Inesperado: " + ch.toChar())
                return x
            }

            // Función para analizar una expresión matemática.
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm() // Sumar términos.
                    else if (eat('-'.toInt())) x -= parseTerm() // Restar términos.
                    else return x
                }
            }


            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) x *= parseFactor() // Multiplicar factores.
                    else if (eat('/'.toInt())) x /= parseFactor() // Dividir factores.
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // Si encuentra un '+', se ignora y se continua analizando el siguiente factor.
                if (eat('-'.toInt())) return -parseFactor() // Si encuentra un '-', es negativo el resultado del siguiente factor.
                var x: Double
                val startPos = pos // Registra la posición de inicio para controlar números y funciones.
                if (eat('('.toInt())) {
                    x = parseExpression() // Si encuentra '(', se analiza la expresión dentro de paréntesis.
                    eat(')'.toInt()) // Se espera encontrar ')' que cierra la expresión.
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // Si es un número
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar() // Avanza hasta el final del número.
                    x = str.substring(startPos, pos).toDouble() // Convierte la subcadena numérica en un valor double.
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // Si comienza con una letra, podría ser una función matemática.
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar() // Avanza hasta el final del nombre de la función.
                    val func = str.substring(startPos, pos) // Obtiene el nombre de la función.
                    x = parseFactor()
                    x = if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(Math.toRadians(x)) else if (func == "cos") Math.cos(Math.toRadians(x)) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException("Función desconocida: $func")
                } else {
                    throw RuntimeException("Inesperado: " + ch.toChar()) // Si no coincide con ningún caso conocido, se lanza una excepción.
                }
                if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // Si encuentra '^', se calcula la potencia del factor.
                return x
            }
        }.parse()
    }

}