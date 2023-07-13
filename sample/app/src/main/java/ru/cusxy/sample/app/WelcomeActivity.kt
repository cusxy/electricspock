package ru.cusxy.sample.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View

class WelcomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_activity)

        val button = findViewById<View>(R.id.login)
        button.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}