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
import android.support.annotation.Nullable;

import com.ecompany.antoine.emotionapp.data.Camera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

//import static android.support.v4.util.Preconditions.checkNotNull;


/**
 * Concrete implementation to load cameras from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class CamerasRepository implements CamerasDataSource {

    private static CamerasRepository INSTANCE = null;

    private final CamerasDataSource mCamerasRemoteDataSource;

    private final CamerasDataSource mCamerasLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Camera> mCachedCameras;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private CamerasRepository(@NonNull CamerasDataSource camerasRemoteDataSource,
                              @NonNull CamerasDataSource camerasLocalDataSource) {
        mCamerasRemoteDataSource = checkNotNull(camerasRemoteDataSource);
        mCamerasLocalDataSource = checkNotNull(camerasLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param camerasRemoteDataSource the backend data source
     * @param camerasLocalDataSource  the device storage data source
     * @return the {@link CamerasRepository} instance
     */
    public static CamerasRepository getInstance(CamerasDataSource camerasRemoteDataSource,
                                                CamerasDataSource camerasLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new CamerasRepository(camerasRemoteDataSource, camerasLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(CamerasDataSource, CamerasDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets cameras from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadCamerasCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getCameras(@NonNull final LoadCamerasCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedCameras != null && !mCacheIsDirty) {
            callback.onCamerasLoaded(new ArrayList<>(mCachedCameras.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getCamerasFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mCamerasLocalDataSource.getCameras(new LoadCamerasCallback() {
                @Override
                public void onCamerasLoaded(List<Camera> cameras) {
                    refreshCache(cameras);
                    callback.onCamerasLoaded(new ArrayList<>(mCachedCameras.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getCamerasFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveCamera(@NonNull Camera camera) {
        checkNotNull(camera);
        mCamerasRemoteDataSource.saveCamera(camera);
        mCamerasLocalDataSource.saveCamera(camera);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        mCachedCameras.put(camera.getId(), camera);
    }

    @Override
    public void closeCamera(@NonNull Camera camera) {
        checkNotNull(camera);
        mCamerasRemoteDataSource.closeCamera(camera);
        mCamerasLocalDataSource.closeCamera(camera);

        Camera completedCamera = new Camera(camera.getTitle(), camera.getDescription(), camera.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        mCachedCameras.put(camera.getId(), completedCamera);
    }

    @Override
    public void closeCamera(@NonNull String cameraId) {
        checkNotNull(cameraId);
        closeCamera(getCameraWithId(cameraId));
    }

    @Override
    public void activateCamera(@NonNull Camera camera) {
        checkNotNull(camera);
        mCamerasRemoteDataSource.activateCamera(camera);
        mCamerasLocalDataSource.activateCamera(camera);

        Camera activeCamera = new Camera(camera.getTitle(), camera.getDescription(), camera.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        mCachedCameras.put(camera.getId(), activeCamera);
    }

    @Override
    public void activateCamera(@NonNull String cameraId) {
        checkNotNull(cameraId);
        activateCamera(getCameraWithId(cameraId));
    }

    @Override
    public void clearClosedCameras() {
        mCamerasRemoteDataSource.clearClosedCameras();
        mCamerasLocalDataSource.clearClosedCameras();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Camera>> it = mCachedCameras.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Camera> entry = it.next();
            if (entry.getValue().isClosed()) {
                it.remove();
            }
        }
    }

    /**
     * Gets cameras from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetCameraCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getCamera(@NonNull final String cameraId, @NonNull final GetCameraCallback callback) {
        checkNotNull(cameraId);
        checkNotNull(callback);

        Camera cachedCamera = getCameraWithId(cameraId);

        // Respond immediately with cache if available
        if (cachedCamera != null) {
            callback.onCameraLoaded(cachedCamera);
            return;
        }

        // Load from server/persisted if needed.

        // Is the camera in the local data source? If not, query the network.
        mCamerasLocalDataSource.getCamera(cameraId, new GetCameraCallback() {
            @Override
            public void onCameraLoaded(Camera camera) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedCameras == null) {
                    mCachedCameras = new LinkedHashMap<>();
                }
                mCachedCameras.put(camera.getId(), camera);
                callback.onCameraLoaded(camera);
            }

            @Override
            public void onDataNotAvailable() {
                mCamerasRemoteDataSource.getCamera(cameraId, new GetCameraCallback() {
                    @Override
                    public void onCameraLoaded(Camera camera) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedCameras == null) {
                            mCachedCameras = new LinkedHashMap<>();
                        }
                        mCachedCameras.put(camera.getId(), camera);
                        callback.onCameraLoaded(camera);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshCameras() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllCameras() {
        mCamerasRemoteDataSource.deleteAllCameras();
        mCamerasLocalDataSource.deleteAllCameras();

        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        mCachedCameras.clear();
    }

    @Override
    public void deleteCamera(@NonNull String cameraId) {
        mCamerasRemoteDataSource.deleteCamera(checkNotNull(cameraId));
        mCamerasLocalDataSource.deleteCamera(checkNotNull(cameraId));

        mCachedCameras.remove(cameraId);
    }

    private void getCamerasFromRemoteDataSource(@NonNull final LoadCamerasCallback callback) {
        mCamerasRemoteDataSource.getCameras(new LoadCamerasCallback() {
            @Override
            public void onCamerasLoaded(List<Camera> cameras) {
                refreshCache(cameras);
                refreshLocalDataSource(cameras);
                callback.onCamerasLoaded(new ArrayList<>(mCachedCameras.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Camera> cameras) {
        if (mCachedCameras == null) {
            mCachedCameras = new LinkedHashMap<>();
        }
        mCachedCameras.clear();
        for (Camera camera : cameras) {
            mCachedCameras.put(camera.getId(), camera);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Camera> cameras) {
        mCamerasLocalDataSource.deleteAllCameras();
        for (Camera camera : cameras) {
            mCamerasLocalDataSource.saveCamera(camera);
        }
    }

    @Nullable
    private Camera getCameraWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedCameras == null || mCachedCameras.isEmpty()) {
            return null;
        } else {
            return mCachedCameras.get(id);
        }
    }
}
