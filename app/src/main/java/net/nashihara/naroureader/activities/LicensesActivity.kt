package net.nashihara.naroureader.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ActivityLicensesBinding

class LicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityLicensesBinding>(this, R.layout.activity_licenses)

        binding.toolbar.title = "オープンソースライセンス"
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding.toolbar.setNavigationOnClickListener { v -> onBackPressed() }
        binding.webView.loadUrl("file:///android_asset/licenses.html")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
