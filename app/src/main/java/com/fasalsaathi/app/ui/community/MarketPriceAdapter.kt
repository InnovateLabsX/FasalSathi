package com.fasalsaathi.app.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.card.MaterialCardView

class MarketPriceAdapter(
    private val onPriceClick: (MarketPrice) -> Unit
) : RecyclerView.Adapter<MarketPriceAdapter.MarketPriceViewHolder>() {

    private var prices = listOf<MarketPrice>()

    fun updatePrices(newPrices: List<MarketPrice>) {
        prices = newPrices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketPriceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_market_price, parent, false)
        return MarketPriceViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarketPriceViewHolder, position: Int) {
        holder.bind(prices[position])
    }

    override fun getItemCount(): Int = prices.size

    inner class MarketPriceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cropNameText: TextView = itemView.findViewById(R.id.tvCropName)
        private val varietyText: TextView = itemView.findViewById(R.id.tvCropVariety)
        private val currentPriceText: TextView = itemView.findViewById(R.id.tvCurrentPrice)
        private val priceChangeText: TextView = itemView.findViewById(R.id.tvPriceChange)
        private val marketText: TextView = itemView.findViewById(R.id.tvMarketLocation)
        private val lastUpdatedText: TextView = itemView.findViewById(R.id.tvLastUpdated)

        fun bind(price: MarketPrice) {
            cropNameText.text = price.cropName
            varietyText.text = price.variety
            currentPriceText.text = price.currentPrice
            priceChangeText.text = price.priceChange
            priceChangeText.setTextColor(
                itemView.context.getColor(
                    if (price.isIncreasing) R.color.success_green else R.color.error_red
                )
            )
            marketText.text = price.market
            lastUpdatedText.text = price.lastUpdated

            itemView.setOnClickListener {
                onPriceClick(price)
            }
        }
    }
}