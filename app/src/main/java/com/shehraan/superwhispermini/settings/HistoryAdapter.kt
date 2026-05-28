package com.shehraan.superwhispermini.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shehraan.superwhispermini.R
import com.shehraan.superwhispermini.formatting.DictationMode
import com.shehraan.superwhispermini.history.DictationHistoryEntry
import com.shehraan.superwhispermini.history.DictationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for history list.
 */
class HistoryAdapter(
    private val onCopyClick: (String) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<DictationHistoryEntry, HistoryAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val finalTextView: TextView = itemView.findViewById(R.id.finalText)
        private val metaTextView: TextView = itemView.findViewById(R.id.metaText)
        private val copyButton: Button = itemView.findViewById(R.id.copyButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        
        fun bind(entry: DictationHistoryEntry) {
            finalTextView.text = entry.finalText
            
            val modeText = when (entry.mode) {
                DictationMode.VOICE -> "Voice"
                DictationMode.MESSAGE -> "Message"
            }
            
            val statusText = when (entry.status) {
                DictationStatus.SUCCESS -> "OK"
                DictationStatus.FAILED -> "Failed"
                DictationStatus.CANCELLED -> "Cancelled"
            }
            
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = dateFormat.format(Date(entry.timestamp))
            
            metaTextView.text = "$modeText • $statusText • ${entry.latencyMillis}ms • $date"
            
            copyButton.setOnClickListener {
                onCopyClick(entry.finalText)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(entry.id)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<DictationHistoryEntry>() {
        override fun areItemsTheSame(
            oldItem: DictationHistoryEntry,
            newItem: DictationHistoryEntry
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: DictationHistoryEntry,
            newItem: DictationHistoryEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}
