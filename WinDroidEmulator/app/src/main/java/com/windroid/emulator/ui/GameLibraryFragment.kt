package com.windroid.emulator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.windroid.emulator.R
import com.windroid.emulator.adapter.GameAdapter
import com.windroid.emulator.databinding.FragmentGameLibraryBinding
import com.windroid.emulator.manager.GameStorageManager

class GameLibraryFragment : Fragment() {
    
    private var _binding: FragmentGameLibraryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var gameAdapter: GameAdapter
    private lateinit var storageManager: GameStorageManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        storageManager = GameStorageManager(requireContext())
        
        setupRecyclerView()
        setupFab()
        loadGames()
    }
    
    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(
            onGameClick = { game ->
                // Open game details or launch game
            },
            onGameLongClick = { game ->
                // Show context menu for backup/uninstall options
            }
        )
        
        binding.recyclerViewGames.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewGames.adapter = gameAdapter
    }
    
    private fun setupFab() {
        binding.fabAddGame.setOnClickListener {
            // Open file picker to select game archive
            showFilePicker()
        }
    }
    
    private fun showFilePicker() {
        // Implement file picker for selecting .zip, .rar files
    }
    
    private fun loadGames() {
        val games = storageManager.getAllGames()
        gameAdapter.submitList(games)
        
        if (games.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }
    
    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.recyclerViewGames.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.recyclerViewGames.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        loadGames()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
