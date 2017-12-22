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

package com.ecompany.antoine.emotionapp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;


import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class CamerasRemoteDataSource implements CamerasDataSource {

    private static CamerasRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Camera> CAMERAS_SERVICE_DATA;

    static {
        CAMERAS_SERVICE_DATA = new LinkedHashMap<>(2);
        addCamera("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addCamera("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    public static CamerasRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CamerasRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private CamerasRemoteDataSource() {}

    private static void addCamera(String title, String description) {
        Camera newCamera = new Camera(title, description);
        CAMERAS_SERVICE_DATA.put(newCamera.getId(), newCamera);
    }

    /**
     * Note: {@link LoadCamerasCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getCameras(final @NonNull LoadCamerasCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onCamerasLoaded(Lists.newArrayList(CAMERAS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetCameraCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getCamera(@NonNull String cameraId, final @NonNull GetCameraCallback callback) {
        final Camera camera = CAMERAS_SERVICE_DATA.get(cameraId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onCameraLoaded(camera);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
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
        // Not required for the remote data source because the {@link CamerasRepository} handles
        // converting from a {@code cameraId} to a {@link camera} using its cached data.
    }

    @Override
    public void activateCamera(@NonNull Camera camera) {
        Camera activeCamera = new Camera(camera.getTitle(), camera.getDescription(), camera.getId());
        CAMERAS_SERVICE_DATA.put(camera.getId(), activeCamera);
    }

    @Override
    public void activateCamera(@NonNull String cameraId) {
        // Not required for the remote data source because the {@link CamerasRepository} handles
        // converting from a {@code cameraId} to a {@link camera} using its cached data.
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

    @Override
    public void refreshCameras() {
        // Not required because the {@link CamerasRepository} handles the logic of refreshing the
        // cameras from all the available data sources.
    }

    @Override
    public void deleteAllCameras() {
        CAMERAS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteCamera(@NonNull String cameraId) {
        CAMERAS_SERVICE_DATA.remove(cameraId);
    }
}
