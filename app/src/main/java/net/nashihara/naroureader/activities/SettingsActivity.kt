package net.nashihara.naroureader.activities

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.balysv.materialmenu.MaterialMenuDrawable

import net.nashihara.naroureader.R
import net.nashihara.naroureader.databinding.ActivitySettingsBinding
import net.nashihara.naroureader.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)

        val materialMenu = MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X)
        binding.toolbar.title = "設定"
        binding.toolbar.navigationIcon = materialMenu
        binding.toolbar.setNavigationOnClickListener { finish() }

        supportFragmentManager.beginTransaction()
                .replace(R.id.settings_content, SettingsFragment())
                .commit()
    }
}
