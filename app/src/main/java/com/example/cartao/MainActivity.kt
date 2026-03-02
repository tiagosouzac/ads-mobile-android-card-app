package com.example.cartao

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private var isShowingFront = true
    private var isFlipping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cardContainer   = findViewById<FrameLayout>(R.id.card_container)
        val cardFrontView   = findViewById<View>(R.id.card_front_view)
        val cardBackView    = findViewById<View>(R.id.card_back_view)

        // Set the camera distance to avoid clipping during flip
        val scale = resources.displayMetrics.density
        cardFrontView.cameraDistance = 8000 * scale
        cardBackView.cameraDistance  = 8000 * scale

        // Tap no cartão ainda vira manualmente
        cardContainer.setOnClickListener {
            if (isFlipping) return@setOnClickListener
            isFlipping = true
            flipCard(cardFrontView, cardBackView)
        }

        // Card text views
        val cardNumber    = cardBackView.findViewById<TextView>(R.id.editTextCardNumber)
        val cardHolder    = cardFrontView.findViewById<TextView>(R.id.textCardHolder)
        val cardExpiry    = cardBackView.findViewById<TextView>(R.id.editTextExpiry)
        val cardCvv       = cardBackView.findViewById<TextView>(R.id.editTextCvv)
        val imgCardBrand  = cardFrontView.findViewById<android.widget.ImageView>(R.id.imgCardBrand)

        fun updateCardBrand(digits: String) {
            val prefix2 = digits.take(2).toIntOrNull() ?: 0
            val prefix4 = digits.take(4).toIntOrNull() ?: 0
            when {
                digits.startsWith("4") -> {
                    imgCardBrand.setImageResource(R.drawable.visa)
                    imgCardBrand.visibility = View.VISIBLE
                }
                prefix2 in 51..55 || prefix4 in 2221..2720 -> {
                    imgCardBrand.setImageResource(R.drawable.mastercard)
                    imgCardBrand.visibility = View.VISIBLE
                }
                else -> {
                    imgCardBrand.setImageDrawable(null)
                    imgCardBrand.visibility = View.INVISIBLE
                }
            }
        }

        // Form fields
        val formCardNumber = findViewById<TextInputEditText>(R.id.formCardNumber)
        val formCardHolder = findViewById<TextInputEditText>(R.id.formCardHolder)
        val formMonth      = findViewById<TextInputEditText>(R.id.formMonth)
        val formYear       = findViewById<TextInputEditText>(R.id.formYear)
        val formCvv        = findViewById<TextInputEditText>(R.id.formCvv)

        // Vira para a face correta conforme o campo em foco
        fun showFront() {
            if (!isShowingFront && !isFlipping) {
                isFlipping = true
                flipCard(cardFrontView, cardBackView)
            }
        }
        fun showBack() {
            if (isShowingFront && !isFlipping) {
                isFlipping = true
                flipCard(cardFrontView, cardBackView)
            }
        }

        // Nome → frente; Número/Mês/Ano/CVV → verso
        formCardHolder.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showFront() }
        formCardNumber.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showBack() }
        formMonth.setOnFocusChangeListener     { _, hasFocus -> if (hasFocus) showBack() }
        formYear.setOnFocusChangeListener      { _, hasFocus -> if (hasFocus) showBack() }
        formCvv.setOnFocusChangeListener       { _, hasFocus -> if (hasFocus) showBack() }

        fun updateExpiry() {
            val month = formMonth.text?.toString().orEmpty().padStart(2, '0').take(2)
            val year  = formYear.text?.toString().orEmpty().padStart(2, '0').take(2)
            cardExpiry.text = when {
                month.isBlank() && year.isBlank() -> "MM/AA"
                else -> "${month.ifEmpty { "MM" }}/${year.ifEmpty { "AA" }}"
            }
        }

        // Número do cartão — formata em grupos de 4, máximo 16 dígitos
        formCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().replace(" ", "").take(16)
                val formatted = digits.chunked(4).joinToString(" ")
                if (s.toString() != formatted) {
                    s.replace(0, s.length, formatted)
                }
                cardNumber.text = formatted.ifEmpty { "0000 0000 0000 0000" }
                updateCardBrand(digits)
                isFormatting = false
            }
        })

        // Nome do titular
        formCardHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                cardHolder.text = s?.toString()?.uppercase()?.ifEmpty { "NOME DO TITULAR" }
            }
        })

        // Mês — valida 01–12 e pula para o campo Ano automaticamente
        formMonth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString()?.toIntOrNull()
                if (value != null && value > 12) s?.replace(0, s.length, "12")
                updateExpiry()
                if (s?.length == 2) formYear.requestFocus()
            }
        })

        // Ano
        formYear.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateExpiry()
            }
        })

        // CVV
        formCvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                cardCvv.text = s?.toString()?.ifEmpty { "•••" }
            }
        })
    }

    private fun flipCard(front: View, back: View) {
        val (outView, inView) = if (isShowingFront) front to back else back to front
        val duration = 200L

        // Phase 1: rotate out view from 0° → 90° (card goes edge-on)
        val rotateOut = ObjectAnimator.ofFloat(outView, View.ROTATION_Y, 0f, 90f).apply {
            this.duration = duration
            interpolator = AccelerateInterpolator()
        }

        // Phase 2: rotate in view from -90° → 0° (card comes back from edge-on)
        val rotateIn = ObjectAnimator.ofFloat(inView, View.ROTATION_Y, -90f, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
        }

        rotateOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Card is now edge-on — swap faces with no visible flash
                outView.visibility = View.GONE
                inView.visibility = View.VISIBLE
                rotateIn.start()
            }
        })

        rotateIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                isShowingFront = !isShowingFront
                isFlipping = false
            }
        })

        rotateOut.start()
    }
}