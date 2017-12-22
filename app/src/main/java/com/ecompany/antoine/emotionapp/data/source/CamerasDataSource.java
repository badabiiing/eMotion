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

package com.ecompany.antoine.emotionapp.data.source;

import android.support.annotation.NonNull;

import com.ecompany.antoine.emotionapp.data.Camera;

import java.util.List;

/**
 * Main entry point for accessing cameras data.
 * <p>
 * For simplicity, only getCameras() and getCamera() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new camera is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface CamerasDataSource {

    interface LoadCamerasCallback {

        void onCamerasLoaded(List<Camera> cameras);

        void onDataNotAvailable();
    }

    interface GetCameraCallback {

        void onCameraLoaded(Camera camera);

        void onDataNotAvailable();
    }

    void getCameras(@NonNull LoadCamerasCallback callback);

    void getCamera(@NonNull String cameraId, @NonNull GetCameraCallback callback);

    void saveCamera(@NonNull Camera camera);

    void closeCamera(@NonNull Camera camera);

    void closeCamera(@NonNull String cameraId);

    void activateCamera(@NonNull Camera camera);

    void activateCamera(@NonNull String cameraId);

    void clearClosedCameras();

    void refreshCameras();

    void deleteAllCameras();

    void deleteCamera(@NonNull String cameraId);
}
