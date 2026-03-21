package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var tvSubtotal: TextView
    private lateinit var tvHandling: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView

    private val handlingFee = 2.50
    private var estimatedTaxRate = 0.066 // e.g. 6.6% flat tax for UI purposes

    private val cartItems = mutableListOf(
        CartItem(
            id = 1,
            title = "Paracetamol 650mg",
            desc = "Analgesic & Antipyretic",
            price = 4.50,
            quantity = 2
        ),
        CartItem(
            id = 2,
            title = "Vitamin C 1000mg",
            desc = "Immunity Support",
            price = 12.99,
            quantity = 1
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Init views
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvHandling = view.findViewById(R.id.tvHandling)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotal = view.findViewById(R.id.tvTotal)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnContinue = view.findViewById<View>(R.id.btnContinue)
        btnContinue.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, CheckoutShippingFragment())
                .addToBackStack(null)
                .commit()
        }

        val rvCartItems = view.findViewById<RecyclerView>(R.id.rvCartItems)
        val adapter = CartAdapter(cartItems) {
            calculateTotals()
        }
        
        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        rvCartItems.adapter = adapter
        
        calculateTotals()

        return view
    }

    private fun calculateTotals() {
        var subtotal = 0.0
        for (item in cartItems) {
            subtotal += item.price * item.quantity
        }

        val tax = subtotal * estimatedTaxRate
        val total = subtotal + handlingFee + tax

        tvSubtotal.text = formatCurrency(subtotal)
        tvHandling.text = formatCurrency(handlingFee)
        tvTax.text = formatCurrency(tax)
        tvTotal.text = formatCurrency(total)
    }

    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "$%.2f", amount)
    }
}
