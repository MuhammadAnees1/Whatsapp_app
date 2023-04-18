package com.example.whatsapp_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
private CircleImageView userProfileImage;
private FirebaseAuth mAuth;
private TextView userProfileName, userProfileStatus;
private Button sendMessageRequestButton,declineMessageRequestButton ;
private String receiverUserID,Current_Status = "new",sendUserID;
    private DatabaseReference userRef,chatRequestRef,contactRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        sendUserID=mAuth.getCurrentUser().getUid();



        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_massage_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_massage_request_button);

        RetrieveUserInfo();

    }
    private void RetrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){

                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manegeChatRequest();

                } else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manegeChatRequest();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void manegeChatRequest() {


        chatRequestRef.child(sendUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserID)){
                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){

                        Current_Status = "request_sent";
                        sendMessageRequestButton.setText("cancel Chat Request");
                    } else if (request_type.equals("received")) {
                        Current_Status="request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);
                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });

                    }
                }else {
                    contactRef.child(sendUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserID)){
                                Current_Status = "Friends";
                                sendMessageRequestButton.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(!sendUserID.equals(receiverUserID)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if(Current_Status.equals("new")){
                        SendChatRequest();
                    }
                    if (Current_Status.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (Current_Status.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Current_Status.equals("Friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        contactRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                Current_Status = "new";
                                sendMessageRequestButton.setText("send message");
                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);

                            }

                        }
                    });

                }

            }
        });

    }

    private void AcceptChatRequest() {

        contactRef.child(sendUserID).child(receiverUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
           if (task.isSuccessful())  {
               contactRef.child(receiverUserID).child(sendUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())  {

                           chatRequestRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){
                                  chatRequestRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          sendMessageRequestButton.setEnabled(true);
                                          Current_Status = "Friends";
                                          sendMessageRequestButton.setText("Remove this contact");
                                          declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                          declineMessageRequestButton.setEnabled(false);
                                      }
                                  });
                              }

                               }
                           });
                       }
                   }
               });
           }
            }
        });
    }

    private void CancelChatRequest() {
    chatRequestRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
       if(task.isSuccessful()){
           chatRequestRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                    sendMessageRequestButton.setEnabled(true);
                    Current_Status = "new";
                    sendMessageRequestButton.setText("send message");
                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                    declineMessageRequestButton.setEnabled(false);

                    }

               }
           });

       }

        }
    });

    }

    private void SendChatRequest() {
        chatRequestRef.child(sendUserID).child(receiverUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(sendUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendMessageRequestButton.setEnabled(true);
                                        Current_Status = "request_sent";
                                        sendMessageRequestButton.setText("cancel Chat Request");

                                    }
                                }
                            });
                        }
                    }
                });
    }
}