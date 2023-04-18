package com.example.whatsapp_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private RecyclerView groupList;
    private DatabaseReference groupRef;

    public GroupsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupList = groupFragmentView.findViewById(R.id.list_view);
        groupList.setLayoutManager(new LinearLayoutManager(getContext()));
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        return groupFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(groupRef.child("Groups"), Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, GroupViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, GroupViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull GroupViewHolder holder, int position, @NonNull Contact model) {
                        holder.userName.setText(model.getName());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String currentGroupName = getRef(holder.getLayoutPosition()).getKey();
                                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                                groupChatIntent.putExtra("groupName", currentGroupName);
                                startActivity(groupChatIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        GroupViewHolder holder = new GroupViewHolder(view);
                        return holder;
                    }
                };
        groupList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView userName , userStatus ;
        CircleImageView profileImage;


        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
