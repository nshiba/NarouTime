package net.nashihara.naroureader.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.nashihara.naroureader.R;
import net.nashihara.naroureader.databinding.ActivityLicensesBinding;

public class LicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLicensesBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_licenses);

        binding.toolbar.setTitle("オープンソースライセンス");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.webView.loadUrl("file:///android_asset/licenses.html");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
