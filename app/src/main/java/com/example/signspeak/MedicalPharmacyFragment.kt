package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.data.PharmacyProduct
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MedicalPharmacyFragment : Fragment() {

    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medical_pharmacy, container, false)
        
        val rvProducts = view.findViewById<RecyclerView>(R.id.rvProducts)
        
        adapter = ProductAdapter(emptyList())
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.adapter = adapter

        val tvCartBadge = view.findViewById<TextView>(R.id.tvCartBadge)
        tvCartBadge.text = "0"

        val flCartContainer = view.findViewById<View>(R.id.flCartContainer)
        flCartContainer.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, CartFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        // Use "medicines" collection as per the Firebase screenshot, not "pharmacy_products"
        db.collection("medicines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.widget.Toast.makeText(requireContext(), "Error loading products: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val productList = mutableListOf<PharmacyProduct>()

                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        android.widget.Toast.makeText(requireContext(), "No products found in Firestore.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    for (doc in snapshot) {
                        try {
                            val product = doc.toObject(PharmacyProduct::class.java)
                            productList.add(product)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(requireContext(), "Error parsing product: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                // Sort client-side to prevent hiding items that don't have a 'createdAt' timestamp in Firestore
                productList.sortByDescending { it.createdAt?.seconds ?: 0L }

                adapter.updateList(productList)
                
                // Keep keeping the cart logic consistent. For now just update badge with total products
                // Or leave it as 0. The original set it to dummyProducts.size
                // val tvCartBadge = view.findViewById<TextView>(R.id.tvCartBadge)
                // tvCartBadge.text = productList.size.toString()
            }
    }
}
