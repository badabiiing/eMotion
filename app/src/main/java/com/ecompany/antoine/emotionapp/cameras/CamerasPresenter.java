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

package com.ecompany.antoine.emotionapp.cameras;

import android.app.Activity;
import android.support.annotation.NonNull;


import com.ecompany.antoine.emotionapp.addeditcamera.AddEditCameraActivity;
import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasRepository;
import com.ecompany.antoine.emotionapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Listens to user actions from the UI ({@link CamerasFragment}), retrieves the data and updates the
 * UI as required.
 */
public class CamerasPresenter implements CamerasContract.Presenter {

    private final CamerasRepository mCamerasRepository;

    private final CamerasContract.View mCamerasView;

    private CamerasFilterType mCurrentFiltering = CamerasFilterType.ALL_CAMERAS;

    private boolean mFirstLoad = true;

    public CamerasPresenter(@NonNull CamerasRepository CamerasRepository, @NonNull CamerasContract.View CamerasView) {
        mCamerasRepository = checkNotNull(CamerasRepository, "camerasRepository cannot be null");
        mCamerasView = checkNotNull(CamerasView, "camerasView cannot be null!");

        mCamerasView.setPresenter(this);
    }

    @Override
    public void start() {
        loadCameras(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a Camera was successfully added, show snackbar
        if (AddEditCameraActivity.REQUEST_ADD_CAMERA == requestCode && Activity.RESULT_OK == resultCode) {
            mCamerasView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadCameras(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadCameras(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link CamerasDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadCameras(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mCamerasView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mCamerasRepository.refreshCameras();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mCamerasRepository.getCameras(new CamerasDataSource.LoadCamerasCallback() {
            @Override
            public void onCamerasLoaded(List<Camera> cameras) {
                List<Camera> camerasToShow = new ArrayList<Camera>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We filter the Cameras based on the requestType
                for (Camera camera : cameras) {
                    switch (mCurrentFiltering) {
                        case ALL_CAMERAS:
                            camerasToShow.add(camera);
                            break;
                        case ACTIVE_CAMERAS:
                            if (camera.isActive()) {
                                camerasToShow.add(camera);
                            }
                            break;
                        case CLOSED_CAMERAS:
                            if (camera.isClosed()) {
                                camerasToShow.add(camera);
                            }
                            break;
                        default:
                            camerasToShow.add(camera);
                            break;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mCamerasView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mCamerasView.setLoadingIndicator(false);
                }

                processCameras(camerasToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mCamerasView.isActive()) {
                    return;
                }
                mCamerasView.showLoadingCamerasError();
            }
        });
    }

    private void processCameras(List<Camera> cameras) {
        if (cameras.isEmpty()) {
            // Show a message indicating there are no Cameras for that filter type.
            processEmptyCameras();
        } else {
            // Show the list of Cameras
            mCamerasView.showCameras(cameras);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_CAMERAS:
                mCamerasView.showActiveFilterLabel();
                break;
            case CLOSED_CAMERAS:
                mCamerasView.showClosedFilterLabel();
                break;
            default:
                mCamerasView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyCameras() {
        switch (mCurrentFiltering) {
            case ACTIVE_CAMERAS:
                mCamerasView.showNoActiveCamera();
                break;
            case CLOSED_CAMERAS:
                mCamerasView.showNoClosedCamera();
                break;
            default:
                mCamerasView.showNoCamera();
                break;
        }
    }

    @Override
    public void addNewCamera() {
        mCamerasView.showAddCamera();
    }

    @Override
    public void openCameraDetails(@NonNull Camera requestedCamera) {
        checkNotNull(requestedCamera, "requestedCamera cannot be null!");
        mCamerasView.showCameraDetailsUi(requestedCamera.getId());
    }

    @Override
    public void closeCamera(@NonNull Camera closedCamera) {
        checkNotNull(closedCamera, "closedCamera cannot be null!");
        mCamerasRepository.closeCamera(closedCamera);
        mCamerasView.showCameraMarkedClosed();
        loadCameras(false, false);
    }

    @Override
    public void activateCamera(@NonNull Camera activeCamera) {
        checkNotNull(activeCamera, "activeCamera cannot be null!");
        mCamerasRepository.activateCamera(activeCamera);
        mCamerasView.showCameraMarkedActive();
        loadCameras(false, false);
    }

    @Override
    public void clearClosedCameras() {
        mCamerasRepository.clearClosedCameras();
        mCamerasView.showClosedCamerasCleared();
        loadCameras(false, false);
    }

    /**
     * Sets the current Camera filtering type.
     *
     * @param requestType Can be {@link CamerasFilterType#ALL_CAMERAS},
     *                    {@link CamerasFilterType#CLOSED_CAMERAS}, or
     *                    {@link CamerasFilterType#ACTIVE_CAMERAS}
     */
    @Override
    public void setFiltering(CamerasFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public CamerasFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
