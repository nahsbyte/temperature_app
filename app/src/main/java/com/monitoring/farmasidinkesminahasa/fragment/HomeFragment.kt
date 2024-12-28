package com.monitoring.farmasidinkesminahasa.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.monitoring.farmasidinkesminahasa.R
import com.monitoring.farmasidinkesminahasa.view.CircularProgressView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Suhu Progress
        val suhuProgress: CircularProgressView = view.findViewById(R.id.suhuProgress)
        val suhuValue: String = "46" // Ganti dengan nilai dinamis suhu
        animateProgress(suhuProgress, suhuValue.toFloat())

        // Kelembapan Progress
        val kelembapanProgress: CircularProgressView = view.findViewById(R.id.kelembapanProgress)
        val kelembapanValue: String = "45" // Ganti dengan nilai dinamis kelembapan
        animateProgress(kelembapanProgress, kelembapanValue.toFloat())

        return view
    }

    private fun animateProgress(progressView: CircularProgressView, targetValue: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetValue)
        animator.duration = 2000 // Durasi animasi
        animator.addUpdateListener { animation ->
            progressView.setProgress(animation.animatedValue as Float)
        }
        animator.start()
    }
}
