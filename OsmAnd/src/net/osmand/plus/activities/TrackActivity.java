/**
 *
 */
package net.osmand.plus.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.osmand.plus.GPXUtilities;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.GpxSelectionHelper;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.myplaces.TrackPointFragment;
import net.osmand.plus.myplaces.TrackRoutePointFragment;
import net.osmand.plus.myplaces.TrackSegmentFragment;
import net.osmand.plus.views.controls.PagerSlidingTabStrip;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

/**
 *
 */
public class TrackActivity extends TabActivity {

	public static final String TRACK_FILE_NAME = "TRACK_FILE_NAME";
	public static final String CURRENT_RECORDING = "CURRENT_RECORDING";
	public static String TAB_PARAM = "TAB_PARAM";
	protected List<WeakReference<Fragment>> fragList = new ArrayList<WeakReference<Fragment>>();
	private File file = null;
	private GPXFile result;
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle icicle) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(icicle);
		Intent intent = getIntent();
		if (intent == null || (!intent.hasExtra(TRACK_FILE_NAME) &&
				!intent.hasExtra(CURRENT_RECORDING))) {
			Log.e("TrackActivity", "Required extra '" + TRACK_FILE_NAME + "' is missing");
			finish();
			return;
		}
		file = null;
		if (intent.hasExtra(TRACK_FILE_NAME)) {
			file = new File(intent.getStringExtra(TRACK_FILE_NAME));
			String fn = file.getName().replace(".gpx", "").replace("/", " ").replace("_", " ");
			getSupportActionBar().setTitle(fn);
		} else {
			getSupportActionBar().setTitle(getString(R.string.currently_recording_track));
		}
		getSupportActionBar().setElevation(0);
		setContentView(R.layout.tab_content);

		PagerSlidingTabStrip mSlidingTabLayout = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);

		mViewPager = (ViewPager) findViewById(R.id.pager);

		setViewPagerAdapter(mViewPager, new ArrayList<TabActivity.TabItem>());
		mSlidingTabLayout.setViewPager(mViewPager);

		new AsyncTask<Void, Void, GPXFile>() {

			protected void onPreExecute() {
				setSupportProgressBarIndeterminateVisibility(true);

			};
			@Override
			protected GPXFile doInBackground(Void... params) {
				if(file == null) {
					return getMyApplication().getSavingTrackHelper().getCurrentGpx();
				}
				return GPXUtilities.loadGPXFile(TrackActivity.this, file);
			}
			protected void onPostExecute(GPXFile result) {
				setSupportProgressBarIndeterminateVisibility(false);

				setResult(result);
				((OsmandFragmentPagerAdapter) mViewPager.getAdapter()).addTab(
						getTabIndicator(R.string.track_segments, TrackSegmentFragment.class));
				if (isHavingTrackPoints()){
					((OsmandFragmentPagerAdapter) mViewPager.getAdapter()).addTab(
							getTabIndicator(R.string.track_points, TrackPointFragment.class));
				}
				if (isHavingRoutePoints()){
					((OsmandFragmentPagerAdapter) mViewPager.getAdapter()).addTab(
							getTabIndicator(R.string.route_points, TrackRoutePointFragment.class));
				}

			};
		}.execute((Void)null);

	}

	protected void setResult(GPXFile result) {
		this.result = result;
	}

	public GPXFile getResult() {
		return result;
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		fragList.add(new WeakReference<Fragment>(fragment));
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public OsmandApplication getMyApplication() {
		return (OsmandApplication) getApplication();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	

	public Toolbar getClearToolbar(boolean visible) {
		final Toolbar tb = (Toolbar) findViewById(R.id.bottomControls);
		tb.setTitle(null);
		tb.getMenu().clear();
		tb.setVisibility(visible? View.VISIBLE : View.GONE);
		return tb;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			return true;

		}
		return false;
	}

	public List<GpxSelectionHelper.GpxDisplayGroup> getContent() {
		GpxSelectionHelper selectedGpxHelper = getMyApplication().getSelectedGpxHelper();
		List<GpxSelectionHelper.GpxDisplayGroup> displayGrous = new ArrayList<GpxSelectionHelper.GpxDisplayGroup>();
		selectedGpxHelper.collectDisplayGroups(displayGrous, getResult());
		return displayGrous;
	}

	boolean isHavingTrackPoints(){
		List<GpxSelectionHelper.GpxDisplayGroup> groups = getContent();
		for (GpxSelectionHelper.GpxDisplayGroup group : groups){
			GpxSelectionHelper.GpxDisplayItemType type = group.getType();
			if (type == GpxSelectionHelper.GpxDisplayItemType.TRACK_POINTS &&
					!group.getModifiableList().isEmpty()){
				return true;
			}
		}

		return false;
	}

	boolean isHavingRoutePoints(){
		List<GpxSelectionHelper.GpxDisplayGroup> groups = getContent();
		for (GpxSelectionHelper.GpxDisplayGroup group : groups){
			GpxSelectionHelper.GpxDisplayItemType type = group.getType();
			if (type == GpxSelectionHelper.GpxDisplayItemType.TRACK_ROUTE_POINTS &&
					!group.getModifiableList().isEmpty()){
				return true;
			}
		}

		return false;
	}

}
