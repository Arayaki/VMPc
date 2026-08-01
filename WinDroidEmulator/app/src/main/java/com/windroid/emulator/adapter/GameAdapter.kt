package com.windroid.emulator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.windroid.emulator.databinding.ItemGameBinding
import com.windroid.emulator.model.Game
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GameAdapter(
    private val onGameClick: (Game) -> Unit,
    private val onGameLongClick: (Game) -> Unit
) : ListAdapter<Game, GameAdapter.GameViewHolder>(GameDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGameClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGameLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(game: Game) {
            binding.apply {
                textViewGameName.text = game.name
                
                // Load game icon or cover image
                val imagePath = game.coverImagePath ?: game.iconPath
                if (!imagePath.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(imagePath)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(imageViewGameCover)
                } else {
                    imageViewGameCover.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                // Show play time
                if (game.playTime > 0) {
                    textViewPlayTime.text = formatPlayTime(game.playTime)
                    textViewPlayTime.visibility = android.view.View.VISIBLE
                } else {
                    textViewPlayTime.visibility = android.view.View.GONE
                }

                // Show last played date
                if (game.lastPlayed > 0) {
                    textViewLastPlayed.text = "Last played: ${formatDate(game.lastPlayed)}"
                    textViewLastPlayed.visibility = android.view.View.VISIBLE
                } else {
                    textViewLastPlayed.visibility = android.view.View.GONE
                }

                // Show game size
                textViewGameSize.text = formatSize(game.gameSize)
            }
        }

        private fun formatPlayTime(minutes: Long): String {
            val hours = minutes / 60
            val mins = minutes % 60
            return if (hours > 0) {
                "${hours}h ${mins}m"
            } else {
                "${mins}m"
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes >= 1073741824 -> String.format("%.2f GB", bytes / 1073741824.0)
                bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576.0)
                bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }

    class GameDiffCallback : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem == newItem
        }
    }
}
