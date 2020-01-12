package snowf0x.github.io.wishes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.jetradarmobile.snowfall.SnowfallView;

public class Wishes extends AppCompatActivity implements OnGestureListener {

    private PageFlipView mPageFlipView;
    private GestureDetector mGestureDetector;
    private MediaPlayer mediaPlayer;
    private SnowfallView snow;
    private MediaPlayer.OnPreparedListener preList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPageFlipView = new PageFlipView(this, getIntent().getStringArrayListExtra("URIs"));

        setContentView(mPageFlipView);
        View popup = View.inflate(this, R.layout.activity_wishes, null);
        snow = popup.findViewById(R.id.snow);
        if (getIntent().getBooleanExtra("anim", false))
            snow.setVisibility(View.VISIBLE);
        else
            snow.setVisibility(View.GONE);
        addContentView(popup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mGestureDetector = new GestureDetector(this, this);
        if (!getIntent().getStringExtra("musicURI").equals(""))
            mediaPlayer = MediaPlayer.create(this, Uri.parse(getIntent().getStringExtra("musicURI")));
        else {
            mediaPlayer = MediaPlayer.create(this, R.raw.birthday);
        }

        mediaPlayer.setLooping(true);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        startActivity(new Intent(Wishes.this,Transparent.class)
                .putExtra("desc",getIntent().getStringExtra("desc"))
                .putExtra("title",getIntent().getStringExtra("title"))
                .putExtra("profile",getIntent().getStringExtra("profile")));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("music", false)) {
            mediaPlayer.start();
        }
        mPageFlipView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mPageFlipView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
        mPageFlipView.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mPageFlipView.onFingerUp(event.getX(), event.getY());
            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mPageFlipView.onFingerDown(e.getX(), e.getY());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        mPageFlipView.onFingerMove(e2.getX(), e2.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}

