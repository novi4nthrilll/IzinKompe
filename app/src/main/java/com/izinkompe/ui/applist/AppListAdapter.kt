package com.izinkompe.ui.applist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.izinkompe.data.model.AppInfo
import com.izinkompe.databinding.ItemAppBinding

class AppListAdapter(
    private val onCheckedChanged: (Int, Boolean) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    inner class AppViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo, position: Int) {
            binding.tvAppName.text = appInfo.appName
            binding.tvPackageName.text = appInfo.packageName
            binding.ivAppIcon.setImageDrawable(appInfo.icon)

            // Avoid triggering listener during bind
            binding.cbSelected.setOnCheckedChangeListener(null)
            binding.cbSelected.isChecked = appInfo.isSelected

            binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(position, isChecked)
            }

            // Clicking the whole row toggles the checkbox
            binding.root.setOnClickListener {
                binding.cbSelected.isChecked = !binding.cbSelected.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
