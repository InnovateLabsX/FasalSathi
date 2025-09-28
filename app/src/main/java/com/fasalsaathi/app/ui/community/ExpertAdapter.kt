package com.fasalsaathi.app.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.card.MaterialCardView

class ExpertAdapter(
    private val onExpertClick: (ExpertConsultation) -> Unit
) : RecyclerView.Adapter<ExpertAdapter.ExpertViewHolder>() {

    private var experts = listOf<ExpertConsultation>()

    fun updateExperts(newExperts: List<ExpertConsultation>) {
        experts = newExperts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expert, parent, false)
        return ExpertViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpertViewHolder, position: Int) {
        holder.bind(experts[position])
    }

    override fun getItemCount(): Int = experts.size

    inner class ExpertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tvExpertName)
        private val specializationText: TextView = itemView.findViewById(R.id.tvExpertSpecialization)
        private val ratingText: TextView = itemView.findViewById(R.id.tvExpertRating)
        private val feeText: TextView = itemView.findViewById(R.id.tvConsultationFee)
        private val statusText: TextView = itemView.findViewById(R.id.tvAvailabilityStatus)
        private val languagesText: TextView = itemView.findViewById(R.id.tvExpertLanguages)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvExpertDescription)

        fun bind(expert: ExpertConsultation) {
            nameText.text = expert.expertName
            specializationText.text = expert.specialization
            ratingText.text = expert.rating.toString()
            feeText.text = expert.consultationFee
            statusText.text = if (expert.isAvailable) "Available" else "Busy"
            statusText.setTextColor(
                itemView.context.getColor(
                    if (expert.isAvailable) R.color.success_green else R.color.error_red
                )
            )
            languagesText.text = expert.languages.joinToString(", ")
            descriptionText.text = expert.description

            itemView.setOnClickListener {
                onExpertClick(expert)
            }
        }
    }
}