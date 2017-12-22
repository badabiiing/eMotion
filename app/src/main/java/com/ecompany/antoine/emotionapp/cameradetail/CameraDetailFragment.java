/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ecompany.antoine.emotionapp.cameradetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ecompany.antoine.emotionapp.R;
import com.ecompany.antoine.emotionapp.addeditcamera.AddEditCameraActivity;
import com.ecompany.antoine.emotionapp.addeditcamera.AddEditCameraFragment;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the camera detail screen.
 */
public class CameraDetailFragment extends Fragment implements CameraDetailContract.View {

    @NonNull
    private static final String ARGUMENT_CAMERA_ID = "CAMERA_ID";

    @NonNull
    private static final int REQUEST_EDIT_CAMERA = 1;

    private CameraDetailContract.Presenter mPresenter;

    private TextView mDetailTitle;

    private TextView mDetailDescription;

    private CheckBox mDetailClosedStatus;

    public static CameraDetailFragment newInstance(@Nullable String cameraId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_CAMERA_ID, cameraId);
        CameraDetailFragment fragment = new CameraDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.cameradetail_frag, container, false);
        setHasOptionsMenu(true);
        mDetailTitle = (TextView) root.findViewById(R.id.camera_detail_title);
        mDetailDescription = (TextView) root.findViewById(R.id.camera_detail_description);
        mDetailClosedStatus = (CheckBox) root.findViewById(R.id.camera_detail_closed);

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_camera);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.editCamera();
            }
        });

        return root;
    }

    @Override
    public void setPresenter(@NonNull CameraDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mPresenter.deleteCamera();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cameradetail_fragment_menu, menu);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));
        }
    }

    @Override
    public void hideDescription() {
        mDetailDescription.setVisibility(View.GONE);
    }

    @Override
    public void hideTitle() {
        mDetailTitle.setVisibility(View.GONE);
    }

    @Override
    public void showDescription(@NonNull String description) {
        mDetailDescription.setVisibility(View.VISIBLE);
        mDetailDescription.setText(description);
    }

    @Override
    public void showClosingStatus(final boolean closed) {
        Preconditions.checkNotNull(mDetailClosedStatus);

        mDetailClosedStatus.setChecked(closed);
        mDetailClosedStatus.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mPresenter.closeCamera();
                        } else {
                            mPresenter.activateCamera();
                        }
                    }
                });
    }

    @Override
    public void showEditCamera(@NonNull String cameraId) {
        Intent intent = new Intent(getContext(), AddEditCameraActivity.class);
        intent.putExtra(AddEditCameraFragment.ARGUMENT_EDIT_CAMERA_ID, cameraId);
        startActivityForResult(intent, REQUEST_EDIT_CAMERA);
    }

    @Override
    public void showCameraDeleted() {
        getActivity().finish();
    }

    public void showCameraMarkedClosed() {
        Snackbar.make(getView(), getString(R.string.camera_marked_closed), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showCameraMarkedActive() {
        Snackbar.make(getView(), getString(R.string.camera_marked_active), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_CAMERA) {
            // If the camera was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void showTitle(@NonNull String title) {
        mDetailTitle.setVisibility(View.VISIBLE);
        mDetailTitle.setText(title);
    }

    @Override
    public void showMissingCamera() {
        mDetailTitle.setText("");
        mDetailDescription.setText(getString(R.string.no_data));
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

}
