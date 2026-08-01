package com.windroid.emulator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.windroid.emulator.adapter.ArchiveAdapter
import com.windroid.emulator.databinding.FragmentArchiveExplorerBinding
import com.windroid.emulator.manager.ArchiveExtractor
import java.io.File

class ArchiveExplorerFragment : Fragment() {
    
    private var _binding: FragmentArchiveExplorerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var archiveAdapter: ArchiveAdapter
    private lateinit var extractor: ArchiveExtractor
    
    private var currentArchivePath: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        extractor = ArchiveExtractor(requireContext())
        
        setupRecyclerView()
        
        // Get archive path from arguments
        currentArchivePath = arguments?.getString("archive_path")
        if (currentArchivePath != null) {
            loadArchiveContents(currentArchivePath!!)
        }
    }
    
    private fun setupRecyclerView() {
        archiveAdapter = ArchiveAdapter(
            onFileClick = { file ->
                if (file.isDirectory) {
                    // Navigate into directory or expand folder
                    navigateToDirectory(file.path)
                } else {
                    // Show file options (extract, view info)
                    showFileOptions(file)
                }
            }
        )
        
        binding.recyclerViewArchive.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewArchive.adapter = archiveAdapter
    }
    
    private fun loadArchiveContents(archivePath: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val files = extractor.listArchiveContents(archivePath)
            archiveAdapter.submitList(files)
            binding.progressBar.visibility = View.GONE
            
            binding.textViewArchiveName.text = File(archivePath).name
            binding.textViewFileCount.text = "${files.size} files"
        }
    }
    
    private fun navigateToDirectory(directoryPath: String) {
        // Filter and display contents of the selected directory
        val filteredFiles = archiveAdapter.currentList.filter { 
            it.path.startsWith(directoryPath) && it.path != directoryPath 
        }
        archiveAdapter.submitList(filteredFiles)
    }
    
    private fun showFileOptions(file: com.windroid.emulator.model.ArchiveFile) {
        // Show dialog with options to extract file or view details
        val options = arrayOf("Extract", "Extract To...", "View Info", "Cancel")
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> extractFile(file)
                    1 -> showExtractLocationPicker(file)
                    2 -> showFileInfo(file)
                }
            }
            .show()
    }
    
    private fun extractFile(file: com.windroid.emulator.model.ArchiveFile) {
        // Extract to default location
        lifecycleScope.launch {
            val downloadDir = File(requireContext().getExternalFilesDir(null), "extracted")
            downloadDir.mkdirs()
            
            val result = extractor.extractArchive(currentArchivePath!!, downloadDir.absolutePath)
            
            if (result) {
                android.widget.Toast.makeText(requireContext(), "Extraction complete", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(requireContext(), "Extraction failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showExtractLocationPicker(file: com.windroid.emulator.model.ArchiveFile) {
        // Show directory picker for custom extraction location
    }
    
    private fun showFileInfo(file: com.windroid.emulator.model.ArchiveFile) {
        val sizeFormatted = formatSize(file.size)
        val dateFormatted = formatDate(file.lastModified)
        
        val infoText = """
            Name: ${file.name}
            Type: ${if (file.isDirectory) "Folder" else "File"}
            Size: $sizeFormatted
            Modified: $dateFormatted
            Path: ${file.path}
        """.trimIndent()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("File Information")
            .setMessage(infoText)
            .setPositiveButton("OK", null)
            .show()
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
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
