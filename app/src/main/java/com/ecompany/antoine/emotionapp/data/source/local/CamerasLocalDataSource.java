package com.ecompany.antoine.emotionapp.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;


import com.ecompany.antoine.emotionapp.data.Camera;
import com.ecompany.antoine.emotionapp.data.source.CamerasDataSource;
import com.ecompany.antoine.emotionapp.data.source.local.CamerasPersistenceContract.CameraEntry;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Concrete implementation of a data source as a db.
 */
public class CamerasLocalDataSource implements CamerasDataSource {

    private static CamerasLocalDataSource INSTANCE;

    private CamerasDbHelper mDbHelper;

    // Prevent direct instantiation.
    private CamerasLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        mDbHelper = new CamerasDbHelper(context);
    }

    public static CamerasLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new CamerasLocalDataSource(context);
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadCamerasCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getCameras(@NonNull LoadCamerasCallback callback) {
        List<Camera> cameras = new ArrayList<Camera>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                CameraEntry.COLUMN_NAME_ENTRY_ID,
                CameraEntry.COLUMN_NAME_TITLE,
                CameraEntry.COLUMN_NAME_DESCRIPTION,
                CameraEntry.COLUMN_NAME_CLOSED
        };

        Cursor c = db.query(
                CameraEntry.TABLE_NAME, projection, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String itemId = c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_ENTRY_ID));
                String title = c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_TITLE));
                String description =
                        c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_DESCRIPTION));
                boolean closed =
                        c.getInt(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_CLOSED)) == 1;
                Camera camera = new Camera(title, description, itemId, closed);
                cameras.add(camera);
            }
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (cameras.isEmpty()) {
            // This will be called if the table is new or just empty.
            callback.onDataNotAvailable();
        } else {
            callback.onCamerasLoaded(cameras);
        }

    }

    /**
     * Note: {@link GetCameraCallback#onDataNotAvailable()} is fired if the {@link Camera} isn't
     * found.
     */
    @Override
    public void getCamera(@NonNull String cameraId, @NonNull GetCameraCallback callback) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                CameraEntry.COLUMN_NAME_ENTRY_ID,
                CameraEntry.COLUMN_NAME_TITLE,
                CameraEntry.COLUMN_NAME_DESCRIPTION,
                CameraEntry.COLUMN_NAME_CLOSED
        };

        String selection = CameraEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { cameraId };

        Cursor c = db.query(
                CameraEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        Camera camera = null;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String itemId = c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_ENTRY_ID));
            String title = c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_TITLE));
            String description =
                    c.getString(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_DESCRIPTION));
            boolean closed =
                    c.getInt(c.getColumnIndexOrThrow(CameraEntry.COLUMN_NAME_CLOSED)) == 1;
            camera = new Camera(title, description, itemId, closed);
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (camera != null) {
            callback.onCameraLoaded(camera);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void saveCamera(@NonNull Camera camera) {
        checkNotNull(camera);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CameraEntry.COLUMN_NAME_ENTRY_ID, camera.getId());
        values.put(CameraEntry.COLUMN_NAME_TITLE, camera.getTitle());
        values.put(CameraEntry.COLUMN_NAME_DESCRIPTION, camera.getDescription());
        values.put(CameraEntry.COLUMN_NAME_CLOSED, camera.isClosed());

        db.insert(CameraEntry.TABLE_NAME, null, values);

        db.close();
    }

    @Override
    public void closeCamera(@NonNull Camera camera) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CameraEntry.COLUMN_NAME_CLOSED, true);

        String selection = CameraEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { camera.getId() };

        db.update(CameraEntry.TABLE_NAME, values, selection, selectionArgs);

        db.close();
    }

    @Override
    public void closeCamera(@NonNull String cameraId) {
        // Not required for the local data source because the {@link CamerasRepository} handles
        // converting from a {@code cameraId} to a {@link camera} using its cached data.
    }

    @Override
    public void activateCamera(@NonNull Camera camera) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CameraEntry.COLUMN_NAME_CLOSED, false);

        String selection = CameraEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { camera.getId() };

        db.update(CameraEntry.TABLE_NAME, values, selection, selectionArgs);

        db.close();
    }

    @Override
    public void activateCamera(@NonNull String cameraId) {
        // Not required for the local data source because the {@link CamerasRepository} handles
        // converting from a {@code cameraId} to a {@link camera} using its cached data.
    }

    @Override
    public void clearClosedCameras() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = CameraEntry.COLUMN_NAME_CLOSED + " LIKE ?";
        String[] selectionArgs = { "1" };

        db.delete(CameraEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
    }

    @Override
    public void refreshCameras() {
        // Not required because the {@link CamerasRepository} handles the logic of refreshing the
        // cameras from all the available data sources.
    }

    @Override
    public void deleteAllCameras() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(CameraEntry.TABLE_NAME, null, null);

        db.close();
    }

    @Override
    public void deleteCamera(@NonNull String cameraId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = CameraEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { cameraId };

        db.delete(CameraEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
    }
}
