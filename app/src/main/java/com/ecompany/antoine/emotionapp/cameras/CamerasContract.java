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

import android.support.annotation.NonNull;

import com.ecompany.antoine.emotionapp.BasePresenter;
import com.ecompany.antoine.emotionapp.BaseView;
import com.ecompany.antoine.emotionapp.data.Camera;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface CamerasContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showCameras(List<Camera> cameras);

        void showAddCamera();

        void showCameraDetailsUi(String cameraId);

        void showCameraMarkedClosed();

        void showCameraMarkedActive();

        void showClosedCamerasCleared();

        void showLoadingCamerasError();

        void showNoCamera();

        void showActiveFilterLabel();

        void showClosedFilterLabel();

        void showAllFilterLabel();

        void showNoActiveCamera();

        void showNoClosedCamera();

        void showSuccessfullySavedMessage();

        boolean isActive();

        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadCameras(boolean forceUpdate);

        void addNewCamera();

        void openCameraDetails(@NonNull Camera requestedCamera);

        void closeCamera(@NonNull Camera closedCamera);

        void activateCamera(@NonNull Camera activeCamera);

        void clearClosedCameras();

        void setFiltering(CamerasFilterType requestType);

        CamerasFilterType getFiltering();
    }
}
