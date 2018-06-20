package com.techhounds.frcgameplan.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Handler
import com.techhounds.frcgameplan.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private var pulsingIconAnimation : ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        createPulsingAnimation()

        Handler().postDelayed({
            shrinkIcon()
            launchApp()
        }, 5000)
    }

    fun createPulsingAnimation() {
        pulsingIconAnimation = ObjectAnimator.ofPropertyValuesHolder(splashIcon,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f))
        pulsingIconAnimation!!.duration = 1000
        pulsingIconAnimation!!.repeatCount = ObjectAnimator.INFINITE
        pulsingIconAnimation!!.repeatMode = ObjectAnimator.REVERSE
        pulsingIconAnimation!!.start()
    }

    fun shrinkIcon() {
        pulsingIconAnimation!!.cancel()
        pulsingIconAnimation = ObjectAnimator.ofPropertyValuesHolder(splashIcon,
                PropertyValuesHolder.ofFloat("scaleX", 0f),
                PropertyValuesHolder.ofFloat("scaleY", 0f))
        pulsingIconAnimation!!.duration = 500
        pulsingIconAnimation!!.repeatCount = 0
        pulsingIconAnimation!!.start()
    }

    fun launchApp() {
        Handler().postDelayed({
            startActivity(WhiteboardActivity.createIntent(this))
            finish()
        }, 500)
    }
}
