package com.ecompany.antoine.emotionapp;

import android.content.Context;
import android.support.annotation.NonNull;


import com.ecompany.antoine.emotionapp.data.FakeCamerasRemoteDataSource;
import com.ecompany.antoine.emotionapp.data.source.CamerasRepository;
import com.ecompany.antoine.emotionapp.data.source.local.CamerasLocalDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of mock implementations for
 * {@link CamerasDataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static CamerasRepository provideCamerasRepository(@NonNull Context context) {
        checkNotNull(context);
        return CamerasRepository.getInstance(FakeCamerasRemoteDataSource.getInstance(),
                CamerasLocalDataSource.getInstance(context));
    }
}
