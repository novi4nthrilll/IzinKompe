package com.izinkompe.ui.applist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.izinkompe.IzinKompeApp
import com.izinkompe.R
import com.izinkompe.data.model.AppInfo
import com.izinkompe.data.model.LockedApp
import com.izinkompe.databinding.ActivityAppListBinding
import com.izinkompe.util.BlocklistExclusions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for selecting apps to lock during study sessions.
 * Displays installed launcher apps (excluding system essentials) with search and checkbox selection.
 */
class AppListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppListBinding
    private lateinit var adapter: AppListAdapter
    private val app by lazy { application as IzinKompeApp }
    private var allApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupSaveButton()
        loadApps()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter { position, isChecked ->
            allApps[position].isSelected = isChecked
            updateSelectedCount()
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                val selectedApps = allApps.filter { it.isSelected }.map { appInfo ->
                    LockedApp(
                        packageName = appInfo.packageName,
                        appName = appInfo.appName,
                        isLocked = true
                    )
                }
                // Clear and re-insert
                app.database.lockedAppDao().deleteAll()
                app.database.lockedAppDao().insertAll(selectedApps)
                finish()
            }
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            // Load installed apps on IO thread
            val installedApps = withContext(Dispatchers.IO) {
                getInstalledLauncherApps()
            }

            // Load currently locked apps
            val lockedPackages = withContext(Dispatchers.IO) {
                app.database.lockedAppDao().getLockedPackageNames().toSet()
            }

            // Mark previously locked apps as selected
            allApps = installedApps.map { appInfo ->
                appInfo.copy(isSelected = appInfo.packageName in lockedPackages)
            }.sortedWith(
                compareByDescending<AppInfo> { it.isSelected }
                    .thenBy { it.appName.lowercase() }
            )

            adapter.submitList(allApps)
            updateSelectedCount()
        }
    }

    private fun getInstalledLauncherApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return resolveInfos
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()
                val icon = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
                AppInfo(packageName, appName, icon)
            }
            .filter { !BlocklistExclusions.isExcluded(it.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
    }

    private fun updateSelectedCount() {
        val count = allApps.count { it.isSelected }
        binding.tvSelectedCount.text = getString(R.string.app_selected_count, count)
    }
}
