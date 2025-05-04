package com.example.blooddonor;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseFirestore firestore;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        firestore = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch role from Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");

                        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this, userRole);
                        viewPager.setAdapter(adapter);

                        new TabLayoutMediator(tabLayout, viewPager,
                                (tab, position) -> tab.setText(adapter.getTabTitle(position))
                        ).attach();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {

        private final String role;
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> tabTitles = new ArrayList<>();

        public ProfilePagerAdapter(@NonNull AppCompatActivity activity, String role) {
            super(activity);
            this.role = role;

            fragmentList.add(new AccountFragment());
            tabTitles.add("Account");

            fragmentList.add(new PreferencesFragment());
            tabTitles.add("Preferences");

            if ("donor".equalsIgnoreCase(role)) {
                fragmentList.add(new StatsFragment());
                tabTitles.add("Stats");
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }

        public String getTabTitle(int position) {
            return tabTitles.get(position);
        }
    }
}
