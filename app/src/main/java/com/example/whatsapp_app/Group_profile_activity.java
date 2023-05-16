package com.example.whatsapp_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
public class Group_profile_activity extends AppCompatActivity {
    Button UpdateGroupSettings , addGroupMember;
    private EditText GroupName, groupDiscription;
    private CircleImageView groupProfileImage;
    private String currentUserID,currentGroupName ;
    FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    Toolbar GroupProfileToolBar;
    StorageReference GroupProfileImagesRef;
    private ActivityResultLauncher<String> FGetContentLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

            addGroupMember =findViewById(R.id.add_group_member);
            UpdateGroupSettings =findViewById(R.id.update_Group_button);
        GroupProfileToolBar = findViewById(R.id.group_settings_toolbar);
            GroupName =findViewById(R.id.set_Group_name);
            groupDiscription =findViewById(R.id.set_group_discription);
            groupProfileImage =findViewById(R.id.group_profile_image);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(Group_profile_activity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GroupProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Group Profile Images");
        setSupportActionBar(GroupProfileToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Group Settings");

        addGroupMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(getApplicationContext(), All_Users_Activity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        UpdateGroupSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });
        RetrieveUserInfo();
        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FGetContentLauncher.launch("image/*");
            }
        });
        FGetContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri==null){
                    Toast.makeText(Group_profile_activity.this, "please select image for Upload ", Toast.LENGTH_SHORT).show();
                    Intent settingActivity = new Intent(Group_profile_activity.this, SettingsActivity.class);
                    startActivity(settingActivity);
                }
                if (uri !=null) {
                    StorageReference filePath = GroupProfileImagesRef.child(currentUserID + ".jpg");
                    filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Group_profile_activity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        // your code to save the downloadUrl to the database

                                        RootRef.child("User").child(currentUserID).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(Group_profile_activity.this, "Image saved in Database, Successfully...", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            String message = task.getException().toString();
                                                            Toast.makeText(Group_profile_activity.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                });
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(Group_profile_activity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
    private void UpdateSettings() {
        String setUserName = GroupName.getText().toString();
        String setStatus = groupDiscription.getText().toString();
        currentGroupName = setUserName;// Update currentGroupName with new group name
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> GroupProfileMap = new HashMap<>();
            GroupProfileMap.put("uid", currentUserID);
            GroupProfileMap.put("name", setUserName);
            GroupProfileMap.put("status", setStatus);
            RootRef.child("User").child(currentUserID).updateChildren(GroupProfileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToChatActivity();
                                Toast.makeText(Group_profile_activity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(Group_profile_activity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void RetrieveUserInfo()
    {
        RootRef.child("User").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            GroupName.setText(retrieveUserName);
                            groupDiscription.setText(retrievesStatus);
                            Picasso.get().load(retrieveProfileImage).into(groupProfileImage);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            GroupName.setText(retrieveUserName);
                            groupDiscription.setText(retrievesStatus);
                        }
                        else
                        {
                            Toast.makeText(Group_profile_activity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
    private void SendUserToChatActivity() {
        Intent intent = new Intent(Group_profile_activity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}