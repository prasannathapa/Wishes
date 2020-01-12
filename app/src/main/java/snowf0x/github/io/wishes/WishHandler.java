package snowf0x.github.io.wishes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.Objects;

public class WishHandler extends AppCompatActivity {
    File folder,folder_parent, file, pof;
    GreetingCard greetingCard;
    private TextView tv_title, tv_desc;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_handler);
        tv_title = findViewById(R.id.tv_wish_handler);
        tv_desc = findViewById(R.id.content_wish_handler);
        imageView = findViewById(R.id.iv_wish_handler);
        String data = getIntent().getData().getLastPathSegment();
        if (data != null) {
            String[] id = data.split("@");
            folder = new File(getCacheDir() + File.separator + "Wish" + File.separator + id[0] + File.separator + id[1]);
            folder_parent = new File(getCacheDir()+File.separator+"Wish" + File.separator + id[0]);
            pof = new File(folder_parent,"profile");
            if(pof.exists()){
                Glide.with(getApplicationContext()).load(pof).override(900,900).placeholder(R.drawable.bg_card).error(R.drawable.bg_card).into(imageView);
            }
            StorageReference fs = FirebaseStorage.getInstance().getReference().child("USERS").child(id[0]).child("profile.jpg");
            fs.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Glide.with(getApplicationContext()).load(R.drawable.profile_placeholder).into(imageView);
                }
            });
            if (!folder.exists()) {
                folder.mkdirs();
            }
            FirebaseDatabase.getInstance().getReference().child("USERS").child(id[0]).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        tv_title.setText("with love,\nAnonymous");
                    } else
                        tv_title.setText("with love,\n" + dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            FirebaseDatabase.getInstance().getReference().child("WISH").child(id[0]).child(id[1]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    greetingCard = dataSnapshot.getValue(GreetingCard.class);
                    if (dataSnapshot.getValue() != null)
                        download();
                    else {
                        Toast.makeText(getApplicationContext(), "This Link is not valid", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public void getPages(String uri, final int itr) {
        file = new File(folder, "" + itr);
        if (!file.exists()) {
            tv_desc.setText("getting page: " + itr);
            Ion.with(getApplicationContext()).load(uri).write(file).setCallback(new FutureCallback<File>() {
                @Override
                public void onCompleted(Exception e, File result) {
                    if (itr < greetingCard.getPages().size() - 1) {
                        greetingCard.getPages().set(itr, Uri.fromFile(file).toString());
                        getPages(greetingCard.getPages().get(itr + 1), itr + 1);
                    } else if (itr == greetingCard.getPages().size() - 1) {
                        greetingCard.getPages().set(itr, Uri.fromFile(file).toString());
                        loadMusic();
                    }
                }
            });

        } else {
            if (itr < greetingCard.getPages().size() - 1) {
                greetingCard.getPages().set(itr, Uri.fromFile(file).toString());
                getPages(greetingCard.getPages().get(itr + 1), itr + 1);
            } else if (itr == greetingCard.getPages().size() - 1) {
                greetingCard.getPages().set(itr, Uri.fromFile(file).toString());
                loadMusic();
            }
        }
    }
    private void loadMusic(){
        file = new File(folder,"music");
        tv_desc.setText("getting background music");
        if(!file.exists()) {
            if (greetingCard.isBackgroundMusic()) {
                if (!Objects.equals(greetingCard.getMusicURI(), "")) {
                    Ion.with(getApplicationContext()).load(greetingCard.getMusicURI()).write(file).setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File result) {
                            greetingCard.setMusicURI(Uri.fromFile(file).toString());
                            loadCover();
                        }
                    });
                } else
                    loadCover();
            }
            else
                loadCover();
        }
        else{
            greetingCard.setMusicURI(Uri.fromFile(file).toString());
            loadCover();
        }
    }
    private void loadCover() {
        file = new File(folder,"cover");
        tv_desc.setText("getting cover, Almost done!...");
        if(!file.exists()) {
                if (!Objects.equals(greetingCard.getCoverURI(), "")) {
                    Ion.with(getApplicationContext()).load(greetingCard.getCoverURI()).write(file).setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File result) {
                            greetingCard.setCoverURI(Uri.fromFile(file).toString());
                            finishUp();
                        }
                    });
                }
                else
                    finishUp();
        }
        else{
            greetingCard.setCoverURI(Uri.fromFile(file).toString());
            finishUp();
        }
    }

    private void download() {
        getPages(greetingCard.getPages().get(0),0);
    }
    private  void finishUp() {
        Intent intent = new Intent(WishHandler.this, Wishes.class);
        intent.putExtra("URIs", greetingCard.getPages());
        if (greetingCard.getCoverURI() != null)
            intent.putExtra("profile", greetingCard.getCoverURI());
        intent.putExtra("title", greetingCard.getTitle());
        intent.putExtra("desc", greetingCard.getMessage());
        intent.putExtra("anim", greetingCard.isShowAnimation());
        intent.putExtra("music", greetingCard.isBackgroundMusic());
        intent.putExtra("musicURI", greetingCard.getMusicURI());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}
