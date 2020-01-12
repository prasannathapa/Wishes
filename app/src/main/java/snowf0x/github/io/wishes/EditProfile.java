package snowf0x.github.io.wishes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 81;
    private Uri filePath;
    private CircleImageView imageView;
    private EditText editText;
    private DatabaseReference database;
    private SharedPreferences sharedPreferences;
    private StorageReference storage;
    private boolean done1, done,done3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editText = findViewById(R.id.profile_name);
        imageView = findViewById(R.id.profice_picture);
        sharedPreferences = getSharedPreferences(MainActivity.CONFIG, MODE_PRIVATE);
        final File file = new File(getFilesDir(), "profile");
        done3 = true;
        if(!getIntent().getBooleanExtra("FINISH", false))
        {
            done3 = false;
            FirebaseStorage.getInstance().getReference().child("USERS").child(sharedPreferences.getString("UID","ERROR"))
                    .child("profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Ion.with(EditProfile.this).load(uri.toString()).write(file).setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File result) {
                            Glide.with(EditProfile.this).load(file).override(800,800).placeholder(R.drawable.profile_placeholder).into(imageView);
                            done3 = true;
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    done3=true;
                }
            });
        }
        else if (file.exists()) {
            Glide.with(this).load(file).override(800,800).placeholder(R.drawable.profile_placeholder).into(imageView);
        }
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        editText.setText(sharedPreferences.getString("NAME", ""));
    }

    public void loadPhoto(View view) {
        if (done3) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
        else
            Snackbar.make(imageView,"Please wait",Snackbar.LENGTH_SHORT).show();
    }

    public void uploadPhoto(View view) {
        if (done3) {
            done = done1 = false;
            if (!editText.getText().toString().equals("")) {
                sharedPreferences.edit().putString("NAME", editText.getText().toString()).apply();
                database
                        .child("USERS")
                        .child(sharedPreferences.getString("UID", "ERROR"))
                        .child("name")
                        .setValue(editText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        done = true;
                        if (done1 || filePath == null) {
                            if (!getIntent().getBooleanExtra("FINISH", false))
                                startActivity(new Intent(EditProfile.this, MyFeed.class));
                            finish();
                        }
                    }
                });
                if (filePath != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    File file = new File(getFilesDir(), "profile");
                    InputStream in = null;
                    try {
                        in = getContentResolver().openInputStream(filePath);
                        OutputStream out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StorageReference ref = storage.child("USERS/" + sharedPreferences.getString("UID", "ERROR") + "/profile.jpg");
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    done1 = true;
                                    if (done && !getIntent().getBooleanExtra("FINISH", false)) {
                                        startActivity(new Intent(EditProfile.this, MyFeed.class));
                                    }
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditProfile.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
                }

            } else
                editText.setError("Name can't be empty");
        }
        else
            Snackbar.make(imageView,"Please wait",Snackbar.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!getIntent().getBooleanExtra("FINISH", false))
            uploadPhoto(null);
        else
            super.onBackPressed();
    }
}
