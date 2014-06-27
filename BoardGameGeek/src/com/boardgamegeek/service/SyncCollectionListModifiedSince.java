package com.boardgamegeek.service;

import static com.boardgamegeek.util.LogUtils.LOGI;
import static com.boardgamegeek.util.LogUtils.makeLogTag;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SyncResult;
import android.text.TextUtils;

import com.boardgamegeek.R;
import com.boardgamegeek.io.Adapter;
import com.boardgamegeek.io.BggService;
import com.boardgamegeek.io.RemoteExecutor;
import com.boardgamegeek.model.CollectionResponse;
import com.boardgamegeek.model.persister.CollectionPersister;
import com.boardgamegeek.util.DateTimeUtils;
import com.boardgamegeek.util.PreferencesUtils;

public class SyncCollectionListModifiedSince extends SyncTask {
	private static final String TAG = makeLogTag(SyncCollectionListModifiedSince.class);

	@Override
	public void execute(RemoteExecutor executor, Account account, SyncResult syncResult) throws IOException,
		XmlPullParserException {
		AccountManager accountManager = AccountManager.get(executor.getContext());
		long date = getLong(account, accountManager, SyncService.TIMESTAMP_COLLECTION_PARTIAL);

		LOGI(TAG, "Syncing collection list modified since " + new Date(date) + "...");
		try {
			String[] statuses = PreferencesUtils.getSyncStatuses(executor.getContext());
			if (statuses == null || statuses.length == 0) {
				LOGI(TAG, "...no statuses set to sync");
				return;
			}

			long lastFullSync = getLong(account, accountManager, SyncService.TIMESTAMP_COLLECTION_COMPLETE);
			if (DateTimeUtils.howManyHoursOld(lastFullSync) < 3) {
				LOGI(TAG, "...skipping; we just did a complete sync");
			}

			CollectionPersister persister = new CollectionPersister(executor.getContext()).includeStats();
			BggService service = Adapter.createWithAuthRetry(executor.getContext());
			Map<String, String> options = new HashMap<String, String>();
			String modifiedSince = BggService.COLLECTION_QUERY_DATE_FORMAT.format(new Date(date));

			boolean cancelled = false;
			for (int i = 0; i < statuses.length; i++) {
				if (isCancelled()) {
					cancelled = true;
					break;
				}
				LOGI(TAG, "...syncing status [" + statuses[i] + "]");

				options.clear();
				options.put(statuses[i], "1");
				options.put(BggService.COLLECTION_QUERY_KEY_STATS, "1");
				options.put(BggService.COLLECTION_QUERY_KEY_MODIFIED_SINCE, modifiedSince);

				CollectionResponse response = getCollectionResponse(service, account.name, options);
				persister.save(response.items);
			}
			if (!cancelled) {
				accountManager.setUserData(account, SyncService.TIMESTAMP_COLLECTION_PARTIAL,
					String.valueOf(persister.getTimeStamp()));
			}
		} finally {
			LOGI(TAG, "...complete!");
		}
	}

	@Override
	public int getNotification() {
		return R.string.sync_notification_collection_partial;
	}

	private long getLong(Account account, AccountManager accountManager, String key) {
		String l = accountManager.getUserData(account, key);
		return TextUtils.isEmpty(l) ? 0 : Long.parseLong(l);
	}
}
