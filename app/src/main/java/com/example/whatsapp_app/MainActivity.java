package com.example.whatsapp_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    private ViewPager2 myViewPager;
    private TabLayout myTabLayout;

    private TabsAccessorAdapter viewPagerAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this,"Welcome", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar myToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Whatsapp");


        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabLayout = findViewById(R.id.main_tabs);


        viewPagerAdapter = new TabsAccessorAdapter(this);
        myViewPager.setAdapter(viewPagerAdapter);

//tabLayout
        myTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                myViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        myViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                myTabLayout.getTabAt(position).select();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            SendUserToLoginActivity();

        }
        else {

            VerifyUserExistence();
        }

    }

    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("User").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists()))
                {

//                    Toast.makeText(MainActivity.this,"Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void SendUserToLoginActivity() {

        Intent LoginActivity = new Intent(MainActivity.this, LoginActivity.class);
        LoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginActivity);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            SendUserToLoginActivity();

        }
        if (item.getItemId() == R.id.main_setting_option) {
            SendUserToSettingActivity();

        }
        if (item.getItemId() == R.id.main_find_friend_option) {

            SendUserToFindFriendsActivity();

        }
        if (item.getItemId() == R.id.main_create_groups_option) {
            RequestNewGroup();

        }
        return true;
    }
// creating group on firebase
    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g NeshTech");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){

                    Toast.makeText(MainActivity.this,"Please Enter Group Name",Toast.LENGTH_SHORT).show();
                }
                else {

                    CreateNewGroup(groupName);

                }

            }
        });

       builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    });
            builder.show();
}

    private void CreateNewGroup(final String groupName) {

        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this , groupName+"  is Created successful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToSettingActivity() {

        Intent SettingActivity = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingActivity);
    }
    private void SendUserToFindFriendsActivity() {

        Intent findActivity = new Intent(MainActivity.this, FindFriendsActivity2.class);
        startActivity(findActivity);
    }
}