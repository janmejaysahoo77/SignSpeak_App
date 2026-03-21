package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Data class for Review Items
data class ReviewOrderItem(
    val title: String,
    val desc: String,
    val price: String,
    val tag: String
)

class ReviewOrderFragment : Fragment() {

    private val reviewItems = listOf(
        ReviewOrderItem("Lisinopril", "10mg Tablet • 30 Day Supply", "$12.50", "Refill Available"),
        ReviewOrderItem("Albuterol HFA", "90mcg Inhaler • 1 Unit", "$45.00", "New Prescription")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review_order, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnPlaceOrder = view.findViewById<View>(R.id.btnPlaceOrder)
        btnPlaceOrder.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, OrderSuccessFragment())
                .addToBackStack(null)
                .commit()
        }

        val rvOrderItems = view.findViewById<RecyclerView>(R.id.rvOrderItems)
        rvOrderItems.layoutManager = LinearLayoutManager(requireContext())
        rvOrderItems.adapter = ReviewOrderAdapter(reviewItems)

        return view
    }

    // Inner Adapter for quick integration
    inner class ReviewOrderAdapter(private val items: List<ReviewOrderItem>) :
        RecyclerView.Adapter<ReviewOrderAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvOrderItemTitle)
            val tvDesc: TextView = view.findViewById(R.id.tvOrderItemDesc)
            val tvPrice: TextView = view.findViewById(R.id.tvOrderItemPrice)
            val tvTag: TextView = view.findViewById(R.id.tvOrderItemTag)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvTitle.text = item.title
            holder.tvDesc.text = item.desc
            holder.tvPrice.text = item.price
            holder.tvTag.text = item.tag
            
            // Adjust tag background and text color based on content (Optional UI tuning based on HTML)
            if (item.tag == "New Prescription") {
                holder.tvTag.setBackgroundResource(R.drawable.bg_cyan_circle) // Optional styling placeholder
                // we can just keep it uniform as per xml
            }
        }

        override fun getItemCount() = items.size
    }
}
