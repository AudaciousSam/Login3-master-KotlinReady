package org.faith.bebetter.FriendsPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.faith.bebetter.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.faith.bebetter.YouPage.ProfileActivity;
import org.faith.bebetter.YouPage.User;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    public FriendsFragment() {
        // Required empty public constructor
    }

    private EditText inviteByEmail;
    private EditText mSearchField;
    private RecyclerView mResultList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference emailDatabase;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private ImageButton shareButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        emailDatabase = FirebaseDatabase.getInstance().getReference().child("Emails");
        mSearchField = v.findViewById(R.id.tvSearchBarFriends);
//        inviteByEmail = v.findViewById(R.id.tvInviteByEmail);
        mResultList = v.findViewById(R.id.rvSearchListFriends);
        shareButton = v.findViewById(R.id.imageButtonShare);
        //Vores recyclerview ændre ikke størelse.
        mResultList.setHasFixedSize(true);
        //Gør vores recyclerview linært.
        mResultList.setLayoutManager(new LinearLayoutManager(getActivity()));

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=org.faith.bebetter");
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });

      /*  //Adds a persons email and whom invited to the database.
        inviteByEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Toast.makeText(getActivity(), "We'll take it from here, and send them an invite", Toast.LENGTH_LONG).show();
                    String email = inviteByEmail.getText().toString();

                    mUserDatabase.child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.getValue();
                            emailDatabase.setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    emailDatabase.child(email).child("from"+currentUserId).setValue(name);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    closeKeyboard();
                    return true;
                }
                return false;
            }
        });
*/
        mSearchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    String searchText = mSearchField.getText().toString();
                    firebaseUserSearch(searchText);
                    //inviteByEmail.setVisibility(View.GONE);
                    shareButton.setVisibility(View.GONE);

                    closeKeyboard();
                    return true;
                }
                return false;
                }
        });

        return v;
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            mSearchField.clearFocus();
        }
    }

    private void firebaseUserSearch(final String searchText) {
        Query firebaseSearchQuery = mUserDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff").limitToFirst(10);

        FirebaseRecyclerOptions<User> options=
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(firebaseSearchQuery, User.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendsFragment.UsersViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_user, parent, false));

            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, int position, @NonNull User model) {
                model.setName(model.getName());
                model.setImage_thumbnail(model.getImage_thumbnail());
                usersViewHolder.setDetails(getContext(), model.name, model.image_thumbnail);

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };
        mResultList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        //Here we set all the details of our notification_card_view.
        public void setDetails(Context ctx, String userName, String userImage){
            TextView user_name = mView.findViewById(R.id.textViewCard);
            ImageView user_image = mView.findViewById(R.id.imageViewCard);

            user_name.setText(userName);
            Glide.with(ctx).load(userImage).into(user_image);
        }
    }
}