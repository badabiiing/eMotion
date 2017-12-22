package com.ecompany.antoine.emotionapp.cameradetail;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ecompany.antoine.emotionapp.R;
import com.ecompany.antoine.emotionapp.Injection;
import com.ecompany.antoine.emotionapp.util.ActivityUtils;

/**
 * Displays camera details screen.
 */
public class CameraDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CAMERA_ID = "CAMERA_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cameradetail_act);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        // Get the requested camera id
        String cameraId = getIntent().getStringExtra(EXTRA_CAMERA_ID);

        CameraDetailFragment cameraDetailFragment = (CameraDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (cameraDetailFragment == null) {
            cameraDetailFragment = CameraDetailFragment.newInstance(cameraId);

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    cameraDetailFragment, R.id.contentFrame);
        }

        // Create the presenter
        new CameraDetailPresenter(
                cameraId,
                Injection.provideCamerasRepository(getApplicationContext()),
                cameraDetailFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
