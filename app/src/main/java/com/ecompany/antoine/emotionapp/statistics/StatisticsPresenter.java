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

package com.ecompany.antoine.emotionapp.statistics;

import android.support.annotation.NonNull;

import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasRepository;
import com.ecompany.antoine.emotionapp.util.EspressoIdlingResource;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class StatisticsPresenter implements StatisticsContract.Presenter {

    private final CamerasRepository mCamerasRepository;

    private final StatisticsContract.View mStatisticsView;

    public StatisticsPresenter(@NonNull CamerasRepository camerasRepository,
                               @NonNull StatisticsContract.View statisticsView) {
        mCamerasRepository = checkNotNull(camerasRepository, "camerasRepository cannot be null");
        mStatisticsView = checkNotNull(statisticsView, "StatisticsView cannot be null!");

        mStatisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadStatistics();
    }

    private void loadStatistics() {
        mStatisticsView.setProgressIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mCamerasRepository.getCameras(new CamerasDataSource.LoadCamerasCallback() {
            @Override
            public void onCamerasLoaded(List<Camera> cameras) {
                int activeCameras = 0;
                int completedCameras = 0;

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We calculate number of active and completed cameras
                for (Camera camera : cameras) {
                    if (camera.isClosed()) {
                        completedCameras += 1;
                    } else {
                        activeCameras += 1;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.setProgressIndicator(false);

                mStatisticsView.showStatistics(activeCameras, completedCameras);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView.isActive()) {
                    return;
                }
                mStatisticsView.showLoadingStatisticsError();
            }
        });
    }
}
