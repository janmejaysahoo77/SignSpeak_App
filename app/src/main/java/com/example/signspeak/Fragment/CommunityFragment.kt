package com.example.signspeak.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

class CommunityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        // Setup Stories
        val rvStories = view.findViewById<RecyclerView>(R.id.rvStories)
        rvStories.adapter = CommunityStoryAdapter(listOf(
            CommunityStory("Elena", R.mipmap.ic_launcher),
            CommunityStory("Marcus", R.mipmap.ic_launcher),
            CommunityStory("Sophie", R.mipmap.ic_launcher),
            CommunityStory("Kai", R.mipmap.ic_launcher)
        ))

        // Setup Suggested
        val rvSuggested = view.findViewById<RecyclerView>(R.id.rvSuggested)
        rvSuggested.adapter = CommunitySuggestedAdapter(listOf(
            CommunitySuggested("Maya R.", "3 mutual", R.mipmap.ic_launcher),
            CommunitySuggested("Leo Chen", "Sign Expert", R.mipmap.ic_launcher),
            CommunitySuggested("Jordan K.", "Nearby", R.mipmap.ic_launcher)
        ))

        // Setup Online
        val rvOnline = view.findViewById<RecyclerView>(R.id.rvOnline)
        rvOnline.adapter = CommunityOnlineAdapter(listOf(
            CommunityOnline(R.mipmap.ic_launcher),
            CommunityOnline(R.mipmap.ic_launcher),
            CommunityOnline(R.mipmap.ic_launcher),
            CommunityOnline(R.mipmap.ic_launcher),
            CommunityOnline(R.mipmap.ic_launcher)
        ))

        // Setup Feed
        val rvFeed = view.findViewById<RecyclerView>(R.id.rvFeed)
        rvFeed.adapter = CommunityPostAdapter(listOf(
            CommunityPost(
                "Sarah Jenkins", "2 HOURS AGO",
                "Finally mastered the new signing for 'Innovation'! Excited to share this journey with the community. 🤟✨",
                "1.2k", "84", "12",
                R.mipmap.ic_launcher, R.mipmap.ic_launcher
            ),
            CommunityPost(
                "David Chen", "5 HOURS AGO",
                "Coffee meetups are the best! Check out our highlights from today's weekend gathering. #DeafCulture #CommunityLove",
                "428", "32", "25",
                R.mipmap.ic_launcher, R.mipmap.ic_launcher
            )
        ))

        return view
    }
}
