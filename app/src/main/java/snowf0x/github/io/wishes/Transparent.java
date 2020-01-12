package snowf0x.github.io.wishes;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class Transparent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);
        String ur = getIntent().getStringExtra("profile");
        if(ur != null)
            Glide.with(this).load(Uri.parse(ur)).placeholder(R.drawable.backgound).error(R.drawable.bg_card).into((ImageView) findViewById(R.id.iv));
        ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
        ((TextView)findViewById(R.id.desc)).setText(getIntent().getStringExtra("desc"));

    }

    public void finish(View view) {
        finish();
    }
}
