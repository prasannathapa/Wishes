package snowf0x.github.io.wishes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<GreetingCard> {
    private Context mContext;
    private String uid,name;

    public ListAdapter(Context context, ArrayList<GreetingCard> items, String uid, String name) {
        super(context, R.layout.wish_row, items);
        this.mContext = context;
        this.uid = uid;
        this.name = name;
    }
    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.wish_row, null);
        }

        final GreetingCard p = getItem(position);

        if (p != null) {
            TextView tv = v.findViewById(R.id.lv_name);
            ImageView iv =  v.findViewById(R.id.lv_img);
            ImageButton ib =  v.findViewById(R.id.lv_edit);

            if (tv != null) {
                tv.setText(p.getTitle());
            }

            if (iv != null) {
                Glide.with(mContext)
                        .load(p.getCoverURI())
                        .placeholder(R.drawable.bg_card)
                        .override(860,400)
                        .centerCrop().into(iv);
                iv.setLongClickable(true);
                iv.setClickable(true);
                iv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(mContext)
                                .setTitle("Delete wish?")
                                .setMessage("Are you sure you want to delete this entry?")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((MyFeed)mContext).delete(p);
                                    }
                                })
                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                                .show();
                        return true;
                    }
                });
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String link = "https://snowf0x.github.io/"+uid+"@"+p.getId();
                        Intent intent = new Intent(getContext(), WishHandler.class);
                        intent.setData(Uri.parse(link));
                        getContext().startActivity(intent);
                    }
                });
            }

            if (ib != null) {
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String link = "https://snowf0x.github.io/"+uid+"@"+p.getId();
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", link);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext.getApplicationContext(), "Link Copied!", Toast.LENGTH_SHORT).show();
                        }
                        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                        String message = "Hy dear!\nCheck out what I made for you\n" +
                                "~----------------------------------~\n"+link+"\n~----------------------------------~" +
                                "\n open this is *Wishes* app\n\nwith regards\n*Yours "+name+"*";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                        shareIntent.setType("text/plain");
                        mContext.startActivity(Intent.createChooser(shareIntent,"Share with"));
                    }
                });
            }
        }

        return v;
    }
}