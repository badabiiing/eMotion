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

package com.ecompany.antoine.emotionapp.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;


import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeCamerasRemoteDataSource implements CamerasDataSource {

    private static FakeCamerasRemoteDataSource INSTANCE;

    private static final Map<String, Camera> CAMERAS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeCamerasRemoteDataSource() {}

    public static FakeCamerasRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeCamerasRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getCameras(@NonNull LoadCamerasCallback callback) {
        callback.onCamerasLoaded(Lists.newArrayList(CAMERAS_SERVICE_DATA.values()));
    }

    @Override
    public void getCamera(@NonNull String cameraId, @NonNull GetCameraCallback callback) {
        Camera camera = CAMERAS_SERVICE_DATA.get(cameraId);
        callback.onCameraLoaded(camera);
    }

    @Override
    public void saveCamera(@NonNull Camera camera) {
        CAMERAS_SERVICE_DATA.put(camera.getId(), camera);
    }

    @Override
    public void closeCamera(@NonNull Camera camera) {
        Camera closedCamera = new Camera(camera.getTitle(), camera.getDescription(), camera.getId(), true);
        CAMERAS_SERVICE_DATA.put(camera.getId(), closedCamera);
    }

    @Override
    public void closeCamera(@NonNull String cameraId) {
        // Not required for the remote data source.
    }

    @Override
    public void activateCamera(@NonNull Camera camera) {
        Camera activeCamera = new Camera(camera.getTitle(), camera.getDescription(), camera.getId());
        CAMERAS_SERVICE_DATA.put(camera.getId(), activeCamera);
    }

    @Override
    public void activateCamera(@NonNull String cameraId) {
        // Not required for the remote data source.
    }

    @Override
    public void clearClosedCameras() {
        Iterator<Map.Entry<String, Camera>> it = CAMERAS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Camera> entry = it.next();
            if (entry.getValue().isClosed()) {
                it.remove();
            }
        }
    }

    public void refreshCameras() {
        // Not required because the {@link CamerasRepository} handles the logic of refreshing the
        // cameras from all the available data sources.
    }

    @Override
    public void deleteCamera(@NonNull String cameraId) {
        CAMERAS_SERVICE_DATA.remove(cameraId);
    }

    @Override
    public void deleteAllCameras() {
        CAMERAS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addCameras(Camera... cameras) {
        for (Camera camera : cameras) {
            CAMERAS_SERVICE_DATA.put(camera.getId(), camera);
        }
    }
}
