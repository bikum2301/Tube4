package com.example.newtube.fragment.music;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.newtube.R;
import com.example.newtube.adapter.MusicPagerAdapter; // Import Adapter
import com.google.android.material.tabs.TabLayout;     // Import TabLayout
import com.google.android.material.tabs.TabLayoutMediator; // Import TabLayoutMediator

public class MusicFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MusicPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout_music);
        viewPager = view.findViewById(R.id.view_pager_music);

        // Khởi tạo Adapter cho ViewPager2
        // Sử dụng requireActivity() hoặc getChildFragmentManager() tùy thuộc vào vòng đời bạn muốn quản lý
        pagerAdapter = new MusicPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        // Kết nối TabLayout với ViewPager2 sử dụng TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Đặt tên cho từng tab dựa vào vị trí
                    switch (position) {
                        case 0:
                            tab.setText("Playlist");
                            // tab.setIcon(R.drawable.ic_playlist); // Có thể thêm icon
                            break;
                        case 1:
                            tab.setText("Bài hát");
                            break;
                        case 2:
                            tab.setText("Album");
                            break;
                        case 3:
                            tab.setText("Nghệ sĩ");
                            break;
                    }
                }
        ).attach(); // Quan trọng: gọi attach() để kết nối

        // (Tùy chọn) Giữ các Fragment con không bị hủy khi vuốt xa
        viewPager.setOffscreenPageLimit(3); // Giữ 3 trang liền kề không bị hủy (ngoài trang hiện tại)
    }
}