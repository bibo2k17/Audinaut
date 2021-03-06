package net.nullsum.audinaut.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.nullsum.audinaut.R;
import net.nullsum.audinaut.adapter.MainAdapter;
import net.nullsum.audinaut.adapter.SectionAdapter;
import net.nullsum.audinaut.util.Constants;
import net.nullsum.audinaut.util.EnvironmentVariables;
import net.nullsum.audinaut.util.FileUtil;
import net.nullsum.audinaut.util.LoadingTask;
import net.nullsum.audinaut.util.ProgressListener;
import net.nullsum.audinaut.util.UserUtil;
import net.nullsum.audinaut.util.Util;
import net.nullsum.audinaut.service.MusicService;
import net.nullsum.audinaut.service.MusicServiceFactory;
import net.nullsum.audinaut.view.UpdateView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragment extends SelectRecyclerFragment<Integer> {
	private static final String TAG = MainFragment.class.getSimpleName();
	public static final String SONGS_LIST_PREFIX = "songs-";
	public static final String SONGS_NEWEST = SONGS_LIST_PREFIX + "newest";
	public static final String SONGS_TOP_PLAYED = SONGS_LIST_PREFIX + "topPlayed";
	public static final String SONGS_RECENT = SONGS_LIST_PREFIX + "recent";
	public static final String SONGS_FREQUENT = SONGS_LIST_PREFIX + "frequent";

	public MainFragment() {
		super();
		pullToRefresh = false;
		serialize = false;
		backgroundUpdate = false;
		alwaysFullscreen = true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		menuInflater.inflate(R.menu.main, menu);
		onFinishSetupOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(super.onOptionsItemSelected(item)) {
			return true;
		}

		return false;
	}

	@Override
	public int getOptionsMenu() {
		return 0;
	}

	@Override
	public SectionAdapter getAdapter(List objs) {
		List<List<Integer>> sections = new ArrayList<>();
		List<String> headers = new ArrayList<>();

		List<Integer> albums = new ArrayList<>();
		albums.add(R.string.main_albums_random);
        albums.add(R.string.main_albums_alphabetical);
		albums.add(R.string.main_albums_genres);
		albums.add(R.string.main_albums_year);
		albums.add(R.string.main_albums_recent);
		albums.add(R.string.main_albums_frequent);

		sections.add(albums);
		headers.add("albums");

		return new MainAdapter(context, headers, sections, this);
	}

	@Override
	public List<Integer> getObjects(MusicService musicService, boolean refresh, ProgressListener listener) throws Exception {
		return Arrays.asList(0);
	}

	@Override
	public int getTitleResource() {
		return R.string.common_appname;
	}

	private void showAlbumList(String type) {
		if("genres".equals(type)) {
			SubsonicFragment fragment = new SelectGenreFragment();
			replaceFragment(fragment);
		} else if("years".equals(type)) {
			SubsonicFragment fragment = new SelectYearFragment();
			replaceFragment(fragment);
		} else {
			// Clear out recently added count when viewing
			if("newest".equals(type)) {
				SharedPreferences.Editor editor = Util.getPreferences(context).edit();
				editor.putInt(Constants.PREFERENCES_KEY_RECENT_COUNT + Util.getActiveServer(context), 0);
				editor.commit();
			}
			
			SubsonicFragment fragment = new SelectDirectoryFragment();
			Bundle args = new Bundle();
			args.putString(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_TYPE, type);
			args.putInt(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_SIZE, 20);
			args.putInt(Constants.INTENT_EXTRA_NAME_ALBUM_LIST_OFFSET, 0);
			fragment.setArguments(args);

			replaceFragment(fragment);
		}
	}

	private void showAboutDialog() {
		new LoadingTask<Void>(context) {
			Long[] used;
			long bytesTotalFs;
			long bytesAvailableFs;

			@Override
			protected Void doInBackground() throws Throwable {
				File rootFolder = FileUtil.getMusicDirectory(context);
				StatFs stat = new StatFs(rootFolder.getPath());
				bytesTotalFs = (long) stat.getBlockCount() * (long) stat.getBlockSize();
				bytesAvailableFs = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();

				used = FileUtil.getUsedSize(context, rootFolder);
				return null;
			}

			@Override
			protected void done(Void result) {
				List<Integer> headers = new ArrayList<>();
				List<String> details = new ArrayList<>();

				headers.add(R.string.details_author);
				details.add("Andrew Rabert");

				headers.add(R.string.details_email);
				details.add("ar@nullsum.net");

				try {
					headers.add(R.string.details_version);
					details.add(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
				} catch(Exception e) {
					details.add("");
				}

				Resources res = context.getResources();
				headers.add(R.string.details_files_cached);
				details.add(Long.toString(used[0]));

				headers.add(R.string.details_files_permanent);
				details.add(Long.toString(used[1]));

				headers.add(R.string.details_used_space);
				details.add(res.getString(R.string.details_of, Util.formatLocalizedBytes(used[2], context), Util.formatLocalizedBytes(Util.getCacheSizeMB(context) * 1024L * 1024L, context)));

				headers.add(R.string.details_available_space);
				details.add(res.getString(R.string.details_of, Util.formatLocalizedBytes(bytesAvailableFs, context), Util.formatLocalizedBytes(bytesTotalFs, context)));

				Util.showDetailsDialog(context, R.string.main_about_title, headers, details);
			}
		}.execute();
	}

	@Override
	public void onItemClicked(UpdateView<Integer> updateView, Integer item) {
		if (item == R.string.main_albums_random) {
			showAlbumList("random");
		} else if (item == R.string.main_albums_recent) {
			showAlbumList("recent");
		} else if (item == R.string.main_albums_frequent) {
			showAlbumList("frequent");
		} else if(item == R.string.main_albums_genres) {
			showAlbumList("genres");
		} else if(item == R.string.main_albums_year) {
			showAlbumList("years");
		} else if(item == R.string.main_albums_alphabetical) {
			showAlbumList("alphabeticalByName");
		} else if (item == R.string.main_songs_newest) {
			showAlbumList(SONGS_NEWEST);
		} else if (item == R.string.main_songs_top_played) {
			showAlbumList(SONGS_TOP_PLAYED);
		} else if (item == R.string.main_songs_recent) {
			showAlbumList(SONGS_RECENT);
		} else if (item == R.string.main_songs_frequent) {
			showAlbumList(SONGS_FREQUENT);
		}
	}

	@Override
	public void onCreateContextMenu(Menu menu, MenuInflater menuInflater, UpdateView<Integer> updateView, Integer item) {}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem, UpdateView<Integer> updateView, Integer item) {
		return false;
	}
}
