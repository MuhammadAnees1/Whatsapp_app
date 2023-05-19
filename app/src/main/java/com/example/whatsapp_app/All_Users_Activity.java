package com.example.whatsapp_app;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.annotations.NonNull;
public class All_Users_Activity extends AppCompatActivity {
    RecyclerView usersList;
    private DatabaseReference UsersRef, GroupRef;
    FirebaseAuth mAuth;
    String currentUserID, groupName;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("User");
        groupName = getIntent().getExtras().get("groupName").toString();

        Toast.makeText(All_Users_Activity.this, groupName, Toast.LENGTH_SHORT).show();
      GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        usersList = findViewById(R.id.private_list_of_users);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        mToolbar = findViewById(R.id.Add_User_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("All Contact");

//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(All_Users_Activity.this, GroupChatActivity.class));
//            }
//        });
        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(UsersRef, Contact.class)
                        .build();
        FirebaseRecyclerAdapter<Contact,UsersViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact,UsersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final Contact model) {
                        final String listUserId = getRef(position).getKey();
                        UsersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String userName = dataSnapshot.child("name").getValue().toString();
                                    String userStatus = dataSnapshot.child("status").getValue().toString();
                                    String userProfileImage = dataSnapshot.child("image").getValue().toString();

                                    holder.userName.setText(userName);
                                    holder.userStatus.setText(userStatus);
                                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Map<String, Object> memberData = new HashMap<>();
                                        memberData.put("name", model.getName());
                                        memberData.put("status", model.getStatus());
                                        memberData.put("image", model.getImage());
                                        GroupRef.child("Members").child(listUserId).setValue(memberData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(All_Users_Activity.this, "New member added to group.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(All_Users_Activity.this, "Error occurred while adding member to group.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        return new UsersViewHolder(view);
                    }
                };
        usersList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
