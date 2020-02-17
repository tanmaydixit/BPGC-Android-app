package com.macbitsgoa.bitsgridwatch;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class NotificationIntentService extends IntentService {

    private DatabaseReference databaseReference;
    String uid = "guest";

    public NotificationIntentService(String name) {
        super(name);
    }
    public NotificationIntentService() {
        super("notificationIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        //get db reference
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Cutoff");

        //get user id
        GoogleSignInAccount googleSignInAccount;

        if ((googleSignInAccount = GoogleSignIn.getLastSignedInAccount(
                getApplicationContext())) != null) {
            uid = googleSignInAccount.getId();
        }

        //get key
        String keyId= Objects.requireNonNull(intent.getExtras()).getString("key");

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("NotifIntentService","UID:"+uid+" key:"+keyId);
        switch(Objects.requireNonNull(intent.getAction())){
            case "yes":
                //for yes case
                databaseReference.child("CCAC").child(keyId).child("userResponses").child(uid).setValue("yes");
                Handler leftHandler = new Handler(Looper.getMainLooper());
                leftHandler.post(() -> Toast.makeText(getBaseContext(), "Thank you for your response.",
                        Toast.LENGTH_LONG).show());
                notificationManager.cancelAll();
                Log.e("NotifIntentService","YES case");
                break;
            case "no":
                databaseReference.child("CCAC").child(keyId).child("userResponses").child(uid).setValue("no");
                Handler noHandler = new Handler(Looper.getMainLooper());
                noHandler.post(() -> Toast.makeText(getBaseContext(), "Thank you for your response", Toast.LENGTH_LONG).show());
                notificationManager.cancelAll();
                Log.e("NotifIntentService","NO case");
                break;
            case "maybe":
                databaseReference.child("CCAC").child(keyId).child("userResponses").child(uid).setValue("maybe");
                Handler mayHandler = new Handler(Looper.getMainLooper());
                mayHandler.post(() -> Toast.makeText(getBaseContext(), "Thank you", Toast.LENGTH_LONG).show());
                notificationManager.cancelAll();
                Log.e("NotifIntentService","Maybe case");
                break;
        }
    }
}
