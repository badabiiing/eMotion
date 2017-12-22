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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ecompany.antoine.emotionapp.R;
import com.ecompany.antoine.emotionapp.addeditcamera.AddEditCameraActivity;
import com.ecompany.antoine.emotionapp.cameradetail.CameraDetailActivity;
import com.ecompany.antoine.emotionapp.data.Camera;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Camera}s. User can choose to view all, active or closed cameras.
 */
public class CamerasFragment extends Fragment implements CamerasContract.View {

    private CamerasContract.Presenter mPresenter;

    private CamerasAdapter mListAdapter;

    private View mNoCameraView;

    private ImageView mNoCameraIcon;

    private TextView mNoCameraMainView;

    private TextView mNoCameraAddView;

    private LinearLayout mCamerasView;

    private TextView mFilteringLabelView;

    public CamerasFragment() {
        // Requires empty public constructor
    }

    public static CamerasFragment newInstance() {
        return new CamerasFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new CamerasAdapter(new ArrayList<Camera>(0), mItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull CamerasContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.cameras_frag, container, false);

        // Set up cameras view
        ListView listView = (ListView) root.findViewById(R.id.cameras_list);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        mCamerasView = (LinearLayout) root.findViewById(R.id.camerasLL);

        // Set up  no cameras view
        mNoCameraView = root.findViewById(R.id.noCameras);
        mNoCameraIcon = (ImageView) root.findViewById(R.id.noCamerasIcon);
        mNoCameraMainView = (TextView) root.findViewById(R.id.noCamerasMain);
        mNoCameraAddView = (TextView) root.findViewById(R.id.noCamerasAdd);
        mNoCameraAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCamera();
            }
        });

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_camera);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.addNewCamera();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadCameras(false);
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mPresenter.clearClosedCameras();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadCameras(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cameras_fragment_menu, menu);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_cameras, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(CamerasFilterType.ACTIVE_CAMERAS);
                        break;
                    case R.id.closed:
                        mPresenter.setFiltering(CamerasFilterType.CLOSED_CAMERAS);
                        break;
                    default:
                        mPresenter.setFiltering(CamerasFilterType.ALL_CAMERAS);
                        break;
                }
                mPresenter.loadCameras(false);
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on cameras in the ListView.
     */
    CameraItemListener mItemListener = new CameraItemListener() {
        @Override
        public void onCameraClick(Camera clickedCamera) {
            mPresenter.openCameraDetails(clickedCamera);
        }

        @Override
        public void onClosedCameraClick(Camera closedCamera) {
            mPresenter.closeCamera(closedCamera);
        }

        @Override
        public void onActivateCameraClick(Camera activatedCamera) {
            mPresenter.activateCamera(activatedCamera);
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showCameras(List<Camera> cameras) {
        mListAdapter.replaceData(cameras);

        mCamerasView.setVisibility(View.VISIBLE);
        mNoCameraView.setVisibility(View.GONE);
    }

    @Override
    public void showNoActiveCamera() {
        showNoCameraViews(
                getResources().getString(R.string.no_camera_active),
                R.drawable.ic_check_circle_24dp,
                false
        );
    }

    @Override
    public void showNoCamera() {
        showNoCameraViews(
                getResources().getString(R.string.no_camera_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        );
    }

    @Override
    public void showNoClosedCamera() {
        showNoCameraViews(
                getResources().getString(R.string.no_camera_closed),
                R.drawable.ic_verified_user_24dp,
                false
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_camera_message));
    }

    private void showNoCameraViews(String mainText, int iconRes, boolean showAddView) {
        mCamerasView.setVisibility(View.GONE);
        mNoCameraView.setVisibility(View.VISIBLE);

        mNoCameraMainView.setText(mainText);
        mNoCameraIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoCameraAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showClosedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_closed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showAddCamera() {
        Intent intent = new Intent(getContext(), AddEditCameraActivity.class);
        startActivityForResult(intent, AddEditCameraActivity.REQUEST_ADD_CAMERA);
    }

    @Override
    public void showCameraDetailsUi(String cameraId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        Intent intent = new Intent(getContext(), CameraDetailActivity.class);
        intent.putExtra(CameraDetailActivity.EXTRA_CAMERA_ID, cameraId);
        startActivity(intent);
    }

    @Override
    public void showCameraMarkedClosed() {
        showMessage(getString(R.string.camera_marked_closed));
    }

    @Override
    public void showCameraMarkedActive() {
        showMessage(getString(R.string.camera_marked_active));
    }

    @Override
    public void showClosedCamerasCleared() {
        showMessage(getString(R.string.closed_cameras_cleared));
    }

    @Override
    public void showLoadingCamerasError() {
        showMessage(getString(R.string.loading_cameras_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private static class CamerasAdapter extends BaseAdapter {

        private List<Camera> mCameras;
        private CameraItemListener mItemListener;

        public CamerasAdapter(List<Camera> cameras, CameraItemListener itemListener) {
            setList(cameras);
            mItemListener = itemListener;
        }

        public void replaceData(List<Camera> cameras) {
            setList(cameras);
            notifyDataSetChanged();
        }

        private void setList(List<Camera> cameras) {
            mCameras = checkNotNull(cameras);
        }

        @Override
        public int getCount() {
            return mCameras.size();
        }

        @Override
        public Camera getItem(int i) {
            return mCameras.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.camera_item, viewGroup, false);
            }

            final Camera camera = getItem(i);

            TextView titleTV = (TextView) rowView.findViewById(R.id.title);
            titleTV.setText(camera.getTitleForList());

            CheckBox closeCB = (CheckBox) rowView.findViewById(R.id.closed);

            // Active/closed camera UI
            closeCB.setChecked(camera.isClosed());
            if (camera.isClosed()) {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.list_closed_touch_feedback));
            } else {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback));
            }

            closeCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!camera.isClosed()) {
                        mItemListener.onClosedCameraClick(camera);
                    } else {
                        mItemListener.onActivateCameraClick(camera);
                    }
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onCameraClick(camera);
                }
            });

            return rowView;
        }
    }

    public interface CameraItemListener {

        void onCameraClick(Camera clickedCamera);

        void onClosedCameraClick(Camera closedCamera);

        void onActivateCameraClick(Camera activatedCamera);
    }

}
