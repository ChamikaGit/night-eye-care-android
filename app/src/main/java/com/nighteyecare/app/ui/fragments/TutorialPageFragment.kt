package com.nighteyecare.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nighteyecare.app.R
import com.nighteyecare.app.databinding.FragmentTutorialPage1Binding
import com.nighteyecare.app.databinding.FragmentTutorialPage2Binding
import com.nighteyecare.app.databinding.FragmentTutorialPage3Binding

class TutorialPageFragment : Fragment() {

    private var _binding: Any? = null
    private val binding get() = _binding!!

    private var pageNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View?
        when (pageNumber) {
            1 -> {
                val binding1 = FragmentTutorialPage1Binding.inflate(inflater, container, false)
                binding1.title.text = getString(R.string.what_is_blue_light)
                binding1.content.text = getString(R.string.blue_light_content)
                _binding = binding1
                view = binding1.root
            }
            2 -> {
                val binding2 = FragmentTutorialPage2Binding.inflate(inflater, container, false)
                binding2.title.text = getString(R.string.health_impact)
                binding2.content.text = getString(R.string.health_impact_content)
                _binding = binding2
                view = binding2.root
            }
            3 -> {
                val binding3 = FragmentTutorialPage3Binding.inflate(inflater, container, false)
                binding3.title.text = getString(R.string.how_to_use_app)
                binding3.content.text = getString(R.string.how_to_use_app_content)
                _binding = binding3
                view = binding3.root
            }
            else -> throw IllegalArgumentException("Invalid page number")
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        @JvmStatic
        fun newInstance(pageNumber: Int) = TutorialPageFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PAGE_NUMBER, pageNumber)
            }
        }
    }
}
