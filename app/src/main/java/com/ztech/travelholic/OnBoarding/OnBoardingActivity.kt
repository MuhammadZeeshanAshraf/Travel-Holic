package com.ztech.travelholic.OnBoarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.ztech.travelholic.Activities.HomeActivity
import com.ztech.travelholic.Adapter.OnboardingViewPagerAdapter
import com.ztech.travelholic.R
import com.ztech.travelholic.Utils.Animatoo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ztech.travelholic.Activities.LoginActivity


class OnBoardingActivity : AppCompatActivity() {
    private lateinit var mViewPager: ViewPager
    private lateinit var textSkip: TextView
    var firebaseAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        mViewPager = findViewById(R.id.viewPager)
        mViewPager.adapter = OnboardingViewPagerAdapter(supportFragmentManager, this)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth!!.currentUser
        textSkip = findViewById(R.id.text_skip)
        textSkip.setOnClickListener {
            finish()
            if (currentUser != null) {
                val intent =
                        Intent(applicationContext, HomeActivity::class.java)
                startActivity(intent)
                Animatoo.animateSlideLeft(this)
            } else {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                Animatoo.animateSlideLeft(this)
            }
        }

        val btnNextStep: Button = findViewById(R.id.btn_next_step)

        btnNextStep.setOnClickListener {
            if (getItem(+1) > mViewPager.childCount - 1) {
                finish()
                if (currentUser != null) {
                    val intent =
                            Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    Animatoo.animateSlideLeft(this)
                } else {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    Animatoo.animateSlideLeft(this)
                }
            } else {
                mViewPager.setCurrentItem(getItem(+1), true)
            }
        }

    }

    private fun getItem(i: Int): Int {
        return mViewPager.currentItem + i
    }

}
