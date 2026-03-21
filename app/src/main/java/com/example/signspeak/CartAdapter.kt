package com.example.signspeak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

data class CartItem(
    val id: Int,
    val title: String,
    val desc: String,
    val price: Double,
    var quantity: Int
)

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCartItemTitle: TextView = itemView.findViewById(R.id.tvCartItemTitle)
        val tvCartItemDesc: TextView = itemView.findViewById(R.id.tvCartItemDesc)
        val tvCartItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnIncreaseQty: ImageView = itemView.findViewById(R.id.btnIncreaseQty)
        val btnDecreaseQty: ImageView = itemView.findViewById(R.id.btnDecreaseQty)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.tvCartItemTitle.text = item.title
        holder.tvCartItemDesc.text = item.desc
        holder.tvCartItemPrice.text = String.format(Locale.getDefault(), "$%.2f", item.price)
        holder.tvQuantity.text = item.quantity.toString()

        holder.btnIncreaseQty.setOnClickListener {
            item.quantity++
            holder.tvQuantity.text = item.quantity.toString()
            onCartUpdated()
        }

        holder.btnDecreaseQty.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                holder.tvQuantity.text = item.quantity.toString()
                onCartUpdated()
            }
        }

        holder.btnDelete.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                cartList.removeAt(currentPos)
                notifyItemRemoved(currentPos)
                onCartUpdated()
            }
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }
}
