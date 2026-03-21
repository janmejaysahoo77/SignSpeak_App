package com.example.signspeak

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class Product(
    val id: Int,
    val title: String,
    val desc: String,
    val currentPrice: String,
    val oldPrice: String?,
    val rating: Float,
    val ratingCount: Int,
    val isBestseller: Boolean
)

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBestsellerTag: TextView = itemView.findViewById(R.id.tvBestsellerTag)
        val tvRatingScore: TextView = itemView.findViewById(R.id.tvRatingScore)
        val tvRatingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
        val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        val tvProductDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val tvCurrentPrice: TextView = itemView.findViewById(R.id.tvCurrentPrice)
        val tvOldPrice: TextView = itemView.findViewById(R.id.tvOldPrice)
        val fabAddToCart: FloatingActionButton = itemView.findViewById(R.id.fabAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.tvProductTitle.text = product.title
        holder.tvProductDesc.text = product.desc
        holder.tvCurrentPrice.text = product.currentPrice
        holder.tvRatingScore.text = product.rating.toString()
        holder.tvRatingCount.text = "(${product.ratingCount})"

        if (product.oldPrice != null) {
            holder.tvOldPrice.visibility = View.VISIBLE
            holder.tvOldPrice.text = product.oldPrice
            holder.tvOldPrice.paintFlags = holder.tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvOldPrice.visibility = View.GONE
        }

        if (product.isBestseller) {
            holder.tvBestsellerTag.visibility = View.VISIBLE
        } else {
            holder.tvBestsellerTag.visibility = View.GONE
        }

        holder.fabAddToCart.setOnClickListener {
            Toast.makeText(holder.itemView.context, "${product.title} added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
