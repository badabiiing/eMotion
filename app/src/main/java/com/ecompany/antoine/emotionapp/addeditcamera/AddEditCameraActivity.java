package com.ecompany.antoine.emotionapp.addeditcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ecompany.antoine.emotionapp.R;
import com.ecompany.antoine.emotionapp.Injection;
import com.ecompany.antoine.emotionapp.util.ActivityUtils;
import com.ecompany.antoine.emotionapp.util.EspressoIdlingResource;

/**
 * Displays an add or edit camera screen.
 */
public class AddEditCameraActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_CAMERA = 1;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";

    private AddEditCameraPresenter mAddEditCameraPresenter;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcamera_act);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);

        AddEditCameraFragment addEditCameraFragment = (AddEditCameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        String cameraId = getIntent().getStringExtra(AddEditCameraFragment.ARGUMENT_EDIT_CAMERA_ID);

        setToolbarTitle(cameraId);

        if (addEditCameraFragment == null) {
            addEditCameraFragment = AddEditCameraFragment.newInstance();

            if (getIntent().hasExtra(AddEditCameraFragment.ARGUMENT_EDIT_CAMERA_ID)) {
                Bundle bundle = new Bundle();
                bundle.putString(AddEditCameraFragment.ARGUMENT_EDIT_CAMERA_ID, cameraId);
                addEditCameraFragment.setArguments(bundle);
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    addEditCameraFragment, R.id.contentFrame);
        }

        boolean shouldLoadDataFromRepo = true;

        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

        // Create the presenter
        mAddEditCameraPresenter = new AddEditCameraPresenter(
                cameraId,
                Injection.provideCamerasRepository(getApplicationContext()),
                addEditCameraFragment,
                shouldLoadDataFromRepo);
    }

    private void setToolbarTitle(@Nullable String cameraId) {
        if(cameraId == null) {
            mActionBar.setTitle(R.string.add_camera);
        } else {
            mActionBar.setTitle(R.string.edit_camera);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddEditCameraPresenter.isDataMissing());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
