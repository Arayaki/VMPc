package com.windroid.emulator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windroid.emulator.databinding.ItemArchiveFileBinding
import com.windroid.emulator.model.ArchiveFile
import java.text.SimpleDateFormat
import java.util.*

class ArchiveAdapter(
    private val onFileClick: (ArchiveFile) -> Unit
) : ListAdapter<ArchiveFile, ArchiveAdapter.ArchiveViewHolder>(ArchiveFileDiffCallback()) {

    fun getCurrentList(): List<ArchiveFile> = currentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding = ItemArchiveFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArchiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArchiveViewHolder(
        private val binding: ItemArchiveFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFileClick(getItem(position))
                }
            }
        }

        fun bind(file: ArchiveFile) {
            binding.apply {
                textViewFileName.text = file.name
                
                // Set icon based on file type
                val iconRes = when {
                    file.isDirectory -> android.R.drawable.ic_menu_compass_chat
                    file.name.endsWith(".exe", ignoreCase = true) -> android.R.drawable.ic_menu_save
                    file.name.endsWith("png", "jpg", "jpeg", "gif", ignoreCase = true) -> android.R.drawable.ic_menu_gallery
                    file.name.endsWith("txt", "ini", "cfg", "conf", ignoreCase = true) -> android.R.drawable.ic_menu_edit
                    else -> android.R.drawable.ic_menu_file
                }
                imageViewFileIcon.setImageResource(iconRes)
                
                // Show file size and date
                if (!file.isDirectory) {
                    textViewFileSize.text = formatSize(file.size)
                    textViewFileSize.visibility = android.view.View.VISIBLE
                    textViewFileDate.text = formatDate(file.lastModified)
                    textViewFileDate.visibility = android.view.View.VISIBLE
                } else {
                    textViewFileSize.visibility = android.view.View.GONE
                    textViewFileDate.visibility = android.view.GONE
                }
            }
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes >= 1073741824 -> String.format("%.2f GB", bytes / 1073741824.0)
                bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576.0)
                bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ArchiveFileDiffCallback : DiffUtil.ItemCallback<ArchiveFile>() {
        override fun areItemsTheSame(oldItem: ArchiveFile, newItem: ArchiveFile): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: ArchiveFile, newItem: ArchiveFile): Boolean {
            return oldItem == newItem
        }
    }
}
