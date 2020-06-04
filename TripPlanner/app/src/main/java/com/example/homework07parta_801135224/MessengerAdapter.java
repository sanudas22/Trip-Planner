package com.example.homework07parta_801135224;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;

public class MessengerAdapter extends ArrayAdapter<Message> {

    ArrayList<Message> mData;
    IShareData iShareData;
    Context mContext;

    public MessengerAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Message> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mData = objects;
        this.iShareData = (IShareData) context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Message message = mData.get(position);
        View view;

        if (message.getPost_type()) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_messenger, parent, false);

            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            TextView tv_details = (TextView) view.findViewById(R.id.time_i);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            ImageButton im_comment = (ImageButton) view.findViewById(R.id.im_comment_i);
            ImageButton im_delete = (ImageButton) view.findViewById(R.id.im_delete_i);
            LinearLayout li_comments = (LinearLayout) view.findViewById(R.id.list_comments_i);

            PrettyTime time = new PrettyTime();

            String details = message.getUser_name() + "\n" + time.format(message.getPosted_time());
            tv_details.setText(details);
            Picasso.get().
                    load(message.getImage_url()).
                    placeholder(R.mipmap.ic_loading_placeholder).
                    into(image);

            ArrayList<Post> posts = message.getComments();

            if (posts != null) {
                for (Post post : posts) {

                    View comment_view = inflater.inflate(R.layout.custom_comment, parent, false);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                    TextView tv_message = (TextView) comment_view.findViewById(R.id.tv_message_c);
                    TextView tv_time = (TextView) comment_view.findViewById(R.id.tv_time_c);

                    String details_c = post.getName() + "\n" + time.format(post.getComment_time());
                    tv_message.setText(post.getText());
                    tv_time.setText(details_c);

                    li_comments.addView(comment_view);

                }

            }

            im_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iShareData.deleteMessage(message);
                }
            });

            im_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iShareData.postComment(message);
                }
            });


        } else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_message, parent, false);

            TextView tv_message = (TextView) view.findViewById(R.id.tv_message);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);

            TextView tv_time = (TextView) view.findViewById(R.id.time_m);
            ImageButton im_comment = (ImageButton) view.findViewById(R.id.im_comment_m);
            ImageButton im_delete = (ImageButton) view.findViewById(R.id.im_delete_m);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.list_comments);

            tv_message.setText(message.getText());
            PrettyTime time = new PrettyTime();


            String details = message.getUser_name() + "\n" + time.format(message.getPosted_time());
            tv_time.setText(details);

            im_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iShareData.postComment(message);
                }
            });

            im_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iShareData.deleteMessage(message);
                }
            });

            ArrayList<Post> posts = message.getComments();

            if (posts != null) {
                for (Post post : posts) {

                    View comment_view = inflater.inflate(R.layout.custom_comment, parent, false);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

                    TextView tv_comment = (TextView) comment_view.findViewById(R.id.tv_message_c);
                    TextView tv_time_c = (TextView) comment_view.findViewById(R.id.tv_time_c);

                    String details_c = post.getName() + "\n" + time.format(post.getComment_time());
                    tv_comment.setText(post.getText());
                    tv_time_c.setText(details_c);

                    linearLayout.addView(comment_view);

                }

            }
        }


        return view;

    }

    interface IShareData {
        void postComment(Message message);

        void deleteMessage(Message message);

    }
}
