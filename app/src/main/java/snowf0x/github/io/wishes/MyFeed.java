package snowf0x.github.io.wishes;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class MyFeed extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    BottomAppBar bottomAppBar;
    FloatingActionButton fab;
    DrawerLayout drawer;
    ImageView nav_img,bg_img;
    NavigationView navigationView;
    private TextView email_tv;
    ListView listView;
    ArrayList<GreetingCard> list;
    ListAdapter listAdapter;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_feed);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        listView = findViewById(R.id.lv);
        bg_img = findViewById(R.id.bg_img);
        navigationView = findViewById(R.id.navigation_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, bottomAppBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        sharedPreferences = getSharedPreferences(MainActivity.CONFIG,MODE_PRIVATE);
        list = new ArrayList<>();
        listAdapter = new ListAdapter(this,list,sharedPreferences.getString("UID","ERROR"), sharedPreferences.getString("NAME","your_name"));
        listView.setAdapter(listAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("WISH").child(sharedPreferences.getString("UID","ERROR"));
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GreetingCard greetingCard = dataSnapshot.getValue(GreetingCard.class);
                greetingCard.setId(dataSnapshot.getKey());
                listAdapter.add(greetingCard);
                listAdapter.notifyDataSetChanged();
                bg_img.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        nav_img = header.findViewById(R.id.nav_profile);
        email_tv = header.findViewById(R.id.nav_email);
        /*ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        nav_img.setColorFilter(filter);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(getFilesDir(),"profile");
        if(file.exists()){
            Glide.with(this).load(file).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).placeholder(R.drawable.profile_placeholder).override(800,800).into(nav_img);
        }
        email_tv.setText(sharedPreferences.getString("NAME","YOU"));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile_menu) {
            // Handle the camera action
            startActivity(new Intent(MyFeed.this,EditProfile.class).putExtra("FINISH",true));
        } else if(id == R.id.pages_menu){
            startActivity(new Intent(MyFeed.this,Ads.class));
        }
        else  if(id == R.id.wishes_menu){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("Paste the link here!");
            alert.setTitle("Open a greeting!");
            alert.setIcon(R.drawable.gift);
            alert.setView(edittext);

            alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String uri = edittext.getText().toString();
                    if(uri.length() > 5) {
                        Intent intent = new Intent(MyFeed.this, WishHandler.class);
                        intent.setData(Uri.parse(uri));
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MyFeed.this.getApplicationContext(), "Invalid Link!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });
            alert.show();
        }
        else
            if(id == R.id.settings_menu){
                startActivity(new Intent(MyFeed.this,Settings.class));
            }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void netStory(View view) {

        startActivity(new Intent(MyFeed.this,CreateWish.class));
    }
    ProgressDialog progressDialog;
    StorageReference storageReference;
    public void delete(GreetingCard g) {
        storageReference = FirebaseStorage.getInstance().getReference().child("USERS").child(sharedPreferences.getString("UID","ERROR")).child("WISH").child(g.getId());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Deleting");
        progressDialog.setMessage(g.getTitle());
        progressDialog.setCancelable(false);
        progressDialog.show();
        deleteCache(g.getId());
        deletePages(0,g);
    }
    public void deleteCache(String id) {
        try {
            File dir = new File(getCacheDir()+ File.separator + "WISH"  + File.separator + sharedPreferences.getString("UID", "ERROR") + File.separator + id);
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
    public void deletePages(final int itr, final GreetingCard g){
        storageReference.child(""+itr+".jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(itr < g.getPages().size() - 1)
                    deletePages(itr+1,g);
                else if(itr == g.getPages().size() - 1){
                    deleteMusic(g);
                }
            }
        });

    }

    private void deleteMusic(final GreetingCard g) {
        storageReference.child("audio.mp3").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                storageReference.child("cover.jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReference.child(g.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                listAdapter.remove(g);
                                listAdapter.notifyDataSetChanged();
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                            }
                        });

                    }
                });
            }
        });
    }

    public void share(View view) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        String message = "*Wishes*\n" +
                "~-------------------------------------~\n"
                +"Create virtual albums for your loved ones for free!"
                +"\nhttps://prasannathapa.github.io/wish-app"
                +"\n~-------------------------------------~";
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent,"Share with"));
    }
}
