package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class CheckoutShippingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_checkout_shipping, container, false)

        // Top bar back button
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Bottom bar cancel button
        val btnCancelShipping = view.findViewById<View>(R.id.btnCancelShipping)
        btnCancelShipping.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Bottom bar continue button
        val btnContinuePayment = view.findViewById<View>(R.id.btnContinuePayment)
        btnContinuePayment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, ReviewOrderFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
