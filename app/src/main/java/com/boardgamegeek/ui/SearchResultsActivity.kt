package com.boardgamegeek.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.boardgamegeek.R
import com.boardgamegeek.provider.BggContract.Games
import org.jetbrains.anko.longToast


class SearchResultsActivity : SimpleSinglePaneActivity() {
    companion object {
        private const val KEY_SEARCH_TEXT = "com.boardgamegeek.SEARCH_TEXT"
        private const val ACTION_VOICE_SEARCH = "com.google.android.gms.actions.SEARCH_ACTION"
    }

    private var searchText: String? = null
    private var searchView: SearchView? = null

    override val optionsMenuId: Int
        get() = R.menu.search_widget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            searchText = it.getString(KEY_SEARCH_TEXT)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchText)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        searchView = menu.findItem(R.id.menu_search)?.actionView as SearchView?
        searchView?.let {
            setUpSearchView(it)
            // if searchText was restored from the bundle, populate it in the search view
            if (!searchText.isNullOrEmpty()) {
                it.setQuery(searchText, false)
            }
        }
        return true
    }

    private fun setUpSearchView(searchView: SearchView) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isIconified = false
        searchView.setOnCloseListener {
            finish()
            true
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.length > 1) {
                    (fragment as SearchResultsFragment).search(query)
                }
                // close the auto-complete list; don't pass to a different activity
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchText = newText // save text to be restored in case of a configuration change
                return true
            }
        })
    }

    override fun onCreatePane(intent: Intent): Fragment = SearchResultsFragment.newInstance()

    override fun readIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // selected a search suggestion
                val uri = intent.data
                if (uri == null) {
                    longToast(R.string.search_error_no_data)
                } else {
                    val gameName = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY) ?: ""
                    GameActivity.start(this, Games.getGameId(uri), gameName)
                }
                finish()
            }
            Intent.ACTION_SEARCH,
            ACTION_VOICE_SEARCH -> {
                // searches invoked by the device
                searchText = intent.getStringExtra(SearchManager.QUERY) ?: ""
                searchView?.setQuery(searchText, true)
            }
        }
    }

    override fun getDrawerResId() = R.string.title_search
}
