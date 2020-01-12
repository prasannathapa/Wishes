package snowf0x.github.io.wishes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

public class CreateWish extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private static final int PICK_IMAGE_REQUEST_FOR_PAGES = 974, PICK_IMAGE_REQUEST = 93;
    private static final int PICK_AUDIO_REQUEST_FOR_PAGES = 553;
    private MyRecyclerViewAdapter adapter;
    private ArrayList<String> pagePaths;
    private Uri cover, audio;
    private SwitchCompat s1, s2;
    private ImageView imageView;
    private EditText editText;
    private Button button;
    private EditText editText1;
    private boolean pagesSync = false;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SharedPreferences sp;
    private int PagesLeft;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wish);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        editText = findViewById(R.id.edittext);
        button = findViewById(R.id.custom_music);
        editText1 = findViewById(R.id.msg_short);
        textView = findViewById(R.id.page_desc);
        s1 = findViewById(R.id.sw1);
        s2 = findViewById(R.id.sw2);
        sp = getSharedPreferences(MainActivity.CONFIG, MODE_PRIVATE);
        imageView = findViewById(R.id.header);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(""))
                    collapsingToolbarLayout.setTitle(editable.toString());
                else {
                    collapsingToolbarLayout.setTitle("New Project");
                }
            }
        });
        collapsingToolbarLayout.setTitle("New Project");
        collapsingToolbarLayout.setExpandedTitleColor(Color.rgb(255, 255, 255));
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.rgb(255, 255, 255));
        pagePaths = new ArrayList<>();
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, StaggeredGridLayoutManager.HORIZONTAL, false));
        adapter = new MyRecyclerViewAdapter(this, pagePaths);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        pagePaths.remove(position);
        PagesLeft++;
        textView.setText("You have " + PagesLeft + " Page left");
        adapter.notifyItemRemoved(position);
    }

    public void addCover(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            cover = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cover);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_FOR_PAGES && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            PagesLeft--;
            textView.setText("You have " + PagesLeft + " Page left");
            pagePaths.add(data.getData().toString());
            adapter.notifyItemInserted(pagePaths.size() - 1);
        } else if (requestCode == PICK_AUDIO_REQUEST_FOR_PAGES && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            audio = data.getData();
            button.setText("remove");
            Snackbar.make(collapsingToolbarLayout, "Music added: " + data.getData().getLastPathSegment(), Snackbar.LENGTH_LONG).show();
        }
    }

    public void addPage(View view) {
        if (pagesSync && PagesLeft > 0) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_FOR_PAGES);
        } else getPage(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!pagesSync)
            FirebaseDatabase.getInstance().getReference().child("USERS").child(sp.getString("UID", "ERROR")).child("pages")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue(Integer.class) != null) {
                                pagesSync = true;
                                PagesLeft = dataSnapshot.getValue(Integer.class);
                                textView.setText("You have " + PagesLeft + " Page left");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

    }

    public void preview(View view) {
        if(pagePaths.size()>0) {
            Intent intent = new Intent(CreateWish.this, Wishes.class);
            intent.putExtra("URIs", pagePaths);
            if (cover != null)
                intent.putExtra("profile", cover.toString());
            else
                intent.putExtra("profile", "");
            intent.putExtra("title", editText.getText().toString());
            intent.putExtra("desc", editText1.getText().toString());
            intent.putExtra("anim", s1.isChecked());
            intent.putExtra("music", s2.isChecked());
            if (audio != null)
                intent.putExtra("musicURI", audio.toString());
            else
                intent.putExtra("musicURI", "");

            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Preview should contain at least 1 page", Toast.LENGTH_SHORT).show();
        }
    }

    public void addMusic(View view) {
        if(s2.isChecked()) {
            if (audio == null) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_AUDIO_REQUEST_FOR_PAGES);
            } else {
                audio = null;
                ((Button) view).setText("CUSTOM MUSIC");
                Snackbar.make(collapsingToolbarLayout, "Restored default music", Snackbar.LENGTH_LONG).show();

            }
        }
        else
            Snackbar.make(collapsingToolbarLayout,"Enable Music First!",Snackbar.LENGTH_SHORT).show();
    }

    public void getPage(View view) {
        pagesSync = false;
        startActivity(new Intent(CreateWish.this, Ads.class));
    }

    StorageReference storageReference;
    ProgressDialog progressDialog;
    String key;

    public void upload(View view) {
        if(pagePaths.size() >1) {
            key = FirebaseDatabase.getInstance().getReference().child("WISH").child(sp.getString("UID", "ERROR"))
                    .push().getKey();
            storageReference = FirebaseStorage.getInstance().getReference().child("USERS")
                    .child(sp.getString("UID", "ERROR"))
                    .child("WISH")
                    .child(key);
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            putFile(Uri.parse(pagePaths.get(0)), 0);
        }
        else
            Toast.makeText(this, "Preview should contain at least 2 page", Toast.LENGTH_SHORT).show();

    }

    void done() {
        String aud = "", cov = "", Title = "Untitled", message = "No message written";
        if (audio != null)
            aud = audio.toString();
        if (cover != null)
            cov = cover.toString();
        if (!editText.getText().toString().equals(""))
            Title = editText.getText().toString();
        if (!editText1.getText().toString().equals(""))
            message = editText1.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("WISH").child(sp.getString("UID", "ERROR")).child(key).setValue(new GreetingCard(pagePaths, s1.isChecked(), s2.isChecked(), aud, cov, message, Title)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference().child("USERS").child(sp.getString("UID", "ERROR")).child("pages")
                        .setValue(PagesLeft).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    }
                });

            }
        });
    }

    public void putAudio() {
        progressDialog.setMessage("background audio");
        if (audio != null)
            storageReference.child("audio.mp3").putFile(audio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.child("audio.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            audio = uri;
                            putCover();
                        }


                    });
                }
            });
        else
            putCover();
    }

    private void putCover() {
        progressDialog.setMessage("cover");
        if (cover != null)
            storageReference.child("cover.jpg").putFile(cover)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child("cover.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    cover = uri;
                                    done();
                                }


                            });
                        }
                    });
        else {
            done();
        }
    }

    public void putFile(Uri uri, final int itr) {

        progressDialog.setMessage("page: " + itr);
        storageReference.child("" + itr + ".jpg").putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.child("" + itr + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        pagePaths.set(itr, uri.toString());
                        if (itr < pagePaths.size() - 1)
                            putFile(Uri.parse(pagePaths.get(itr + 1)), itr + 1);
                        else if (itr == pagePaths.size() - 1) {
                            putAudio();
                        }
                    }
                });
            }
        });

    }
}

