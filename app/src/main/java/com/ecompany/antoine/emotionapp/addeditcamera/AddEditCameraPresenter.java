/*
 * Copyright 2016, The Android Open Source Project
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

package com.ecompany.antoine.emotionapp.addeditcamera;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditCameraFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditCameraPresenter implements AddEditCameraContract.Presenter,
        CamerasDataSource.GetCameraCallback {

    @NonNull
    private final CamerasDataSource mCamerasRepository;

    @NonNull
    private final AddEditCameraContract.View mAddCameraView;

    @Nullable
    private String mCameraId;

    private boolean mIsDataMissing;

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param cameraId ID of the camera to edit or null for a new camera
     * @param camerasRepository a repository of data for cameras
     * @param addCameraView the add/edit view
     * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
     */
    public AddEditCameraPresenter(@Nullable String cameraId, @NonNull CamerasDataSource camerasRepository,
                                  @NonNull AddEditCameraContract.View addCameraView, boolean shouldLoadDataFromRepo) {
        mCameraId = cameraId;
        mCamerasRepository = checkNotNull(camerasRepository);
        mAddCameraView = checkNotNull(addCameraView);
        mIsDataMissing = shouldLoadDataFromRepo;

        mAddCameraView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!isNewCamera() && mIsDataMissing) {
            populateCamera();
        }
    }

    @Override
    public void saveCamera(String title, String description) {
        if (isNewCamera()) {
            createCamera(title, description);
        } else {
            updateCamera(title, description);
        }
    }

    @Override
    public void populateCamera() {
        if (isNewCamera()) {
            throw new RuntimeException("populateCamera() was called but camera is new.");
        }
        mCamerasRepository.getCamera(mCameraId, this);
    }

    @Override
    public void onCameraLoaded(Camera camera) {
        // The view may not be able to handle UI updates anymore
        if (mAddCameraView.isActive()) {
            mAddCameraView.setTitle(camera.getTitle());
            mAddCameraView.setDescription(camera.getDescription());
        }
        mIsDataMissing = false;
    }

    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddCameraView.isActive()) {
            mAddCameraView.showEmptyCameraError();
        }
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    private boolean isNewCamera() {
        return mCameraId == null;
    }

    private void createCamera(String title, String description) {
        Camera newCamera = new Camera(title, description);
        if (newCamera.isEmpty()) {
            mAddCameraView.showEmptyCameraError();
        } else {
            mCamerasRepository.saveCamera(newCamera);
            mAddCameraView.showCamerasList();
        }
    }

    private void updateCamera(String title, String description) {
        if (isNewCamera()) {
            throw new RuntimeException("updateCamera() was called but camera is new.");
        }
        mCamerasRepository.saveCamera(new Camera(title, description, mCameraId));
        mAddCameraView.showCamerasList(); // After an edit, go back to the list.
    }
}
