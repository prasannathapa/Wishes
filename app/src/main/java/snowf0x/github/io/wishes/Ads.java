package snowf0x.github.io.wishes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Ads extends AppCompatActivity implements RewardedVideoAdListener {
    private RewardedVideoAd mRewardedVideoAd;
    TextView textView;
    Button button;
    int Pages;
    float amount = 0;
    private SharedPreferences sp;
    boolean morethan1 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        textView = findViewById(R.id.status_text_view);
        button = findViewById(R.id.buttn);
        button.setText("Loading ads...");
        MobileAds.initialize(this, "ca-app-pub-1519774416046235~1105622019");
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        sp = getSharedPreferences(MainActivity.CONFIG,MODE_PRIVATE);
        loadRewardedVideoAd();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
            FirebaseDatabase.getInstance().getReference().child("USERS").child(sp.getString("UID","ERROR")).child("pages")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue(Integer.class) != null){
                                Pages = dataSnapshot.getValue(Integer.class);
                                textView.setText("You have\n"+(Pages+(int)amount)+"\nPage left");
                                FirebaseDatabase.getInstance().getReference().child("USERS").child(sp.getString("UID","ERROR")).child("pages").setValue(((int)amount+Pages)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        amount = 0;
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-1519774416046235/9710870346",
                new AdRequest.Builder().build());
        morethan1 = false;
    }


    @Override
    public void onRewardedVideoAdLeftApplication() {
        if(!morethan1) {
            Toast.makeText(this, "Thanks!", Toast.LENGTH_SHORT).show();
            morethan1 = true;
        }
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(com.google.android.gms.ads.reward.RewardItem rewardItem) {
        amount += 1;
    }



    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "Ads failed to load", Toast.LENGTH_SHORT).show();
        button.setText("Ops, no more ads available!");
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        button.setText("Show Advertisement");
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoCompleted() {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
