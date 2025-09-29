package com.fasalsaathi.app.ui.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.fasalsaathi.app.utils.location.LocationService

class LocationAdapter(
    private val onLocationClick: (LocationService.PlaceResult) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
    
    private var locations = listOf<LocationService.PlaceResult>()
    
    fun updateResults(newLocations: List<LocationService.PlaceResult>) {
        locations = newLocations
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location_result, parent, false)
        return LocationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }
    
    override fun getItemCount() = locations.size
    
    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityName: TextView = itemView.findViewById(R.id.tvCityName)
        private val address: TextView = itemView.findViewById(R.id.tvAddress)
        
        fun bind(place: LocationService.PlaceResult) {
            cityName.text = place.name
            address.text = place.address
            
            itemView.setOnClickListener {
                onLocationClick(place)
            }
        }
    }
}