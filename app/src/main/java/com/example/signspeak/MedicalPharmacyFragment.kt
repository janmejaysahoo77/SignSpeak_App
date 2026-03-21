package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MedicalPharmacyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medical_pharmacy, container, false)
        
        val rvProducts = view.findViewById<RecyclerView>(R.id.rvProducts)
        
        // Dummy data based on the HTML design
        val dummyProducts = listOf(
            Product(
                id = 1,
                title = "Advanced Vitamin C Complex",
                desc = "60 Capsules • 1000mg",
                currentPrice = "$24.99",
                oldPrice = "$32.00",
                rating = 4.8f,
                ratingCount = 124,
                isBestseller = true
            ),
            Product(
                id = 2,
                title = "Glow Hydra Serum",
                desc = "30ml • Clinical Grade",
                currentPrice = "$45.00",
                oldPrice = null,
                rating = 4.9f,
                ratingCount = 86,
                isBestseller = false
            ),
            Product(
                id = 3,
                title = "Bio-Available Zinc",
                desc = "120 Tablets • Plant Based",
                currentPrice = "$18.50",
                oldPrice = "$22.00",
                rating = 4.7f,
                ratingCount = 312,
                isBestseller = false
            ),
            Product(
                id = 4,
                title = "Ultra-Flora Probiotic",
                desc = "30 Servings • 50 Billion CFU",
                currentPrice = "$39.99",
                oldPrice = null,
                rating = 5.0f,
                ratingCount = 21,
                isBestseller = false
            )
        )

        val adapter = ProductAdapter(dummyProducts)
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = adapter

        val tvCartBadge = view.findViewById<TextView>(R.id.tvCartBadge)
        tvCartBadge.text = dummyProducts.size.toString()

        val flCartContainer = view.findViewById<View>(R.id.flCartContainer)
        flCartContainer.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, CartFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
