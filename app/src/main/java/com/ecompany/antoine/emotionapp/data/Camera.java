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
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

/**
 * Immutable model class for a Camera.
 */
public final class Camera {

    @NonNull
    private final String mId;

    @Nullable
    private final String mTitle;

    @Nullable
    private final String mDescription;

    private final boolean mClosed;

    /**
     * Use this constructor to create a new active Camera.
     *
     * @param title       title of the camera
     * @param description description of the camera
     */
    public Camera(@Nullable String title, @Nullable String description) {
        this(title, description, UUID.randomUUID().toString(), false);
    }

    /**
     * Use this constructor to create an active Camera if the Camera already has an id (copy of another
     * Camera).
     *
     * @param title       title of the camera
     * @param description description of the camera
     * @param id          id of the camera
     */
    public Camera(@Nullable String title, @Nullable String description, @NonNull String id) {
        this(title, description, id, false);
    }

    /**
     * Use this constructor to create a new camera Task.
     *
     * @param title       title of the camera
     * @param description description of the camera
     * @param closed   true if the camera is closed, false if it's active
     */
    public Camera(@Nullable String title, @Nullable String description, boolean closed) {
        this(title, description, UUID.randomUUID().toString(), closed);
    }

    /**
     * Use this constructor to specify a closed Camera if the Camera already has an id (copy of
     * another Camera).
     *
     * @param title       title of the camera
     * @param description description of the camera
     * @param id          id of the camera
     * @param closed   true if the camera is closed, false if it's active
     */
    public Camera(@Nullable String title, @Nullable String description,
                  @NonNull String id, boolean closed) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mClosed = closed;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getTitleForList() {
        if (!Strings.isNullOrEmpty(mTitle)) {
            return mTitle;
        } else {
            return mDescription;
        }
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public boolean isClosed() {
        return mClosed;
    }

    public boolean isActive() {
        return !mClosed;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mTitle) &&
               Strings.isNullOrEmpty(mDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Camera camera = (Camera) o;
        return Objects.equal(mId, camera.mId) &&
               Objects.equal(mTitle, camera.mTitle) &&
               Objects.equal(mDescription, camera.mDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mDescription);
    }

    @Override
    public String toString() {
        return "Task with title " + mTitle;
    }
}
