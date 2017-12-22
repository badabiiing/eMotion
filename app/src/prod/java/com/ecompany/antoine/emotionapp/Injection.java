package com.ecompany.antoine.emotionapp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ecompany.antoine.emotionapp.data.source.local.CamerasLocalDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.ecompany.antoine.emotionapp.data.source.remote.CamerasRemoteDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for
 * {@link CamerasDataSource} at compile time.
 */
public class Injection {

    public static CamerasRepository provideCamerasRepository(@NonNull Context context) {
        checkNotNull(context);
        return CamerasRepository.getInstance(CamerasRemoteDataSource.getInstance(),
                CamerasLocalDataSource.getInstance(context));
    }
}
