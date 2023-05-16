package com.example.whatsapp_app;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private EditText userMessageInput;
    String currentGroupName ,currentUserID,currentUserName,currentDate,currentTime ;
    FirebaseAuth mAth;
    DatabaseReference UserRef, GroupNameRef, GroupMessageKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAth = FirebaseAuth.getInstance();
        currentUserID = mAth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("User");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        InitializeFields();
        GetUserInfo();
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this, Group_profile_activity.class);
                intent.putExtra("groupName", currentGroupName);
                startActivity(intent);
            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);
        userMessageInput = findViewById(R.id.input_group_message);
        SendMessageButton = findViewById(R.id.send_massage_button);

        // for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(currentGroupName);
    }
    // for getting user details to firebase
    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupChatActivity", "Error getting user info: " + error.getMessage());
            }
        });
    }
    private void SaveMessageInfoToDatabase() {
    String message = userMessageInput.getText().toString();
    String messageKey = GroupNameRef.push().getKey();
    if(TextUtils.isEmpty(message)){
        Toast.makeText(this, "Please Write message first ",  Toast.LENGTH_SHORT).show();
    }
    else {
        Calendar calForData = Calendar.getInstance();
        SimpleDateFormat currentDataFormat =new SimpleDateFormat("MMM dd ,yyy");
        currentDate = currentDataFormat.format(calForData.getTime());
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat =new SimpleDateFormat("hh : mm : ss");
        currentTime = currentTimeFormat.format(calForTime.getTime());
        HashMap<String , Object> groupMessageKey = new HashMap<>();
        GroupNameRef.updateChildren(groupMessageKey);
        GroupMessageKeyRef = GroupNameRef.child(messageKey);
        HashMap<String , Object> messageInfomap = new HashMap<>();
        messageInfomap.put("name",currentUserName);
        messageInfomap.put("message",message);
        messageInfomap.put("date",currentDate);
        messageInfomap.put("time",currentTime);
        GroupMessageKeyRef.updateChildren(messageInfomap);
    }
    }
//    group massage
private void DisplayMessage(DataSnapshot snapshot) {
    Iterator iterator = snapshot.getChildren().iterator();
    while (iterator.hasNext()) {
        DataSnapshot childSnapshot = (DataSnapshot) iterator.next();
        Object value = childSnapshot.getValue();

        if (value instanceof HashMap) {
            HashMap<String, String> chatDataMap = (HashMap<String, String>) value;

            String chatData = chatDataMap.get("chatData");
            String chatMessage = chatDataMap.get("chatMessage");
            String chatName = chatDataMap.get("chatName");
            String chatTime = chatDataMap.get("chatTime");

            displayTextMessage.append(chatName + ":\n" + chatMessage + ":\n" + chatTime + "   " + chatData + ":\n\n\n");
        } else if (value instanceof String) {
            String chatData = (String) value;
            // Handle the case when the value is a string directly without a HashMap structure
            // You might want to adjust this part based on your specific data structure

            displayTextMessage.append(chatData + "\n");
        }

        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}
}