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

package com.ecompany.antoine.emotionapp.cameradetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasRepository;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link CameraDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class CameraDetailPresenter implements CameraDetailContract.Presenter {

    private final CamerasRepository mCamerasRepository;

    private final CameraDetailContract.View mCameraDetailView;

    @Nullable
    private String mCameraId;

    public CameraDetailPresenter(@Nullable String cameraId,
                                 @NonNull CamerasRepository camerasRepository,
                                 @NonNull CameraDetailContract.View cameraDetailView) {
        mCameraId = cameraId;
        mCamerasRepository = checkNotNull(camerasRepository, "camerasRepository cannot be null!");
        mCameraDetailView = checkNotNull(cameraDetailView, "cameraDetailView cannot be null!");

        mCameraDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        openCamera();
    }

    private void openCamera() {
        if (Strings.isNullOrEmpty(mCameraId)) {
            mCameraDetailView.showMissingCamera();
            return;
        }

        mCameraDetailView.setLoadingIndicator(true);
        mCamerasRepository.getCamera(mCameraId, new CamerasDataSource.GetCameraCallback() {
            @Override
            public void onCameraLoaded(Camera camera) {
                // The view may not be able to handle UI updates anymore
                if (!mCameraDetailView.isActive()) {
                    return;
                }
                mCameraDetailView.setLoadingIndicator(false);
                if (null == camera) {
                    mCameraDetailView.showMissingCamera();
                } else {
                    showCamera(camera);
                }
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mCameraDetailView.isActive()) {
                    return;
                }
                mCameraDetailView.showMissingCamera();
            }
        });
    }

    @Override
    public void editCamera() {
        if (Strings.isNullOrEmpty(mCameraId)) {
            mCameraDetailView.showMissingCamera();
            return;
        }
        mCameraDetailView.showEditCamera(mCameraId);
    }

    @Override
    public void deleteCamera() {
        if (Strings.isNullOrEmpty(mCameraId)) {
            mCameraDetailView.showMissingCamera();
            return;
        }
        mCamerasRepository.deleteCamera(mCameraId);
        mCameraDetailView.showCameraDeleted();
    }

    @Override
    public void closeCamera() {
        if (Strings.isNullOrEmpty(mCameraId)) {
            mCameraDetailView.showMissingCamera();
            return;
        }
        mCamerasRepository.closeCamera(mCameraId);
        mCameraDetailView.showCameraMarkedClosed();
    }

    @Override
    public void activateCamera() {
        if (Strings.isNullOrEmpty(mCameraId)) {
            mCameraDetailView.showMissingCamera();
            return;
        }
        mCamerasRepository.activateCamera(mCameraId);
        mCameraDetailView.showCameraMarkedActive();
    }

    private void showCamera(@NonNull Camera camera) {
        String title = camera.getTitle();
        String description = camera.getDescription();

        if (Strings.isNullOrEmpty(title)) {
            mCameraDetailView.hideTitle();
        } else {
            mCameraDetailView.showTitle(title);
        }

        if (Strings.isNullOrEmpty(description)) {
            mCameraDetailView.hideDescription();
        } else {
            mCameraDetailView.showDescription(description);
        }
        mCameraDetailView.showClosingStatus(camera.isClosed());
    }
}
