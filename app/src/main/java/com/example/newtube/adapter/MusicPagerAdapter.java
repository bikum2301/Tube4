package com.example.newtube.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity; // Sử dụng FragmentActivity hoặc FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newtube.fragment.music.AlbumsFragment;    // Import các fragment con
import com.example.newtube.fragment.music.ArtistsFragment;
import com.example.newtube.fragment.music.PlaylistsFragment;
import com.example.newtube.fragment.music.SongsFragment;

public class MusicPagerAdapter extends FragmentStateAdapter {

    // Có thể truyền số lượng tab vào constructor nếu muốn linh hoạt
    public MusicPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    // Hoặc public MusicPagerAdapter(@NonNull Fragment fragment) { super(fragment); } nếu dùng trong Fragment cha

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về Fragment tương ứng với vị trí tab
        switch (position) {
            case 0:
                return new PlaylistsFragment();
            case 1:
                return new SongsFragment();
            case 2:
                return new AlbumsFragment();
            case 3:
                return new ArtistsFragment();
            default:
                return new PlaylistsFragment(); // Mặc định trả về tab đầu tiên
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Số lượng tab bạn có
    }
}