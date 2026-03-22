package com.example.signspeak

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.data.PharmacyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductAdapter(private var productList: List<PharmacyProduct>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateList(newList: List<PharmacyProduct>) {
        productList = newList
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockStatus: TextView = itemView.findViewById(R.id.tvBestsellerTag)
        val tvRatingScore: TextView = itemView.findViewById(R.id.tvRatingScore)
        val tvRatingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
        val productName: TextView = itemView.findViewById(R.id.tvProductTitle)
        val productDescription: TextView = itemView.findViewById(R.id.tvProductDesc)
        val productPrice: TextView = itemView.findViewById(R.id.tvCurrentPrice)
        val tvOldPrice: TextView = itemView.findViewById(R.id.tvOldPrice)
        val fabAddToCart: FloatingActionButton = itemView.findViewById(R.id.fabAddToCart)
        val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val llRating: View = itemView.findViewById(R.id.llRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        holder.productDescription.text = product.description

        if (product.stock > 0) {
            holder.stockStatus.visibility = View.VISIBLE
            holder.stockStatus.text = "In Stock"
        } else {
            holder.stockStatus.visibility = View.VISIBLE
            holder.stockStatus.text = "Out of Stock"
        }

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .into(holder.productImage)

        // Hiding dummy rating & old price as they are not in the new model
        holder.llRating.visibility = View.GONE
        holder.tvOldPrice.visibility = View.GONE

        holder.fabAddToCart.setOnClickListener {
            Toast.makeText(holder.itemView.context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
