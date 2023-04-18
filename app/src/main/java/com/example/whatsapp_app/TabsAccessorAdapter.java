package com.example.whatsapp_app;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
public class TabsAccessorAdapter extends FragmentStateAdapter {
    public TabsAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ChatsFragment();
            case 1: return new GroupsFragment();
            case 2: return new ContactFragment();
            case 3: return new RequestFragment();
            default: return new ChatsFragment();
        }
    }
    @Override
    public int getItemCount() {
        return 4;
    }
}