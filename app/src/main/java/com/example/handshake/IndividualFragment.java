package com.example.handshake;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IndividualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IndividualFragment extends Fragment {

    RecyclerView individual_recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<IndividualModel> individualList;

    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;

    IndividualAdapter adapter ;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IndividualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IndividualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IndividualFragment newInstance(String param1, String param2) {
        IndividualFragment fragment = new IndividualFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        View v = inflater.inflate(R.layout.fragment_individual, container, false);
        individual_recyclerView = (RecyclerView)v.findViewById(R.id.individual_recyclerView);
        individual_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.individual_swipeContainer);

        individualList = new ArrayList<>();
        adapter = new IndividualAdapter(getContext(),individualList);
        individual_recyclerView.setAdapter(adapter);

        fetchData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                individualList.clear();
                fetchData();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        return v;
    }

    private void fetchData() {
        firebaseDatabase.getReference().child("Individual").child(firebaseAuth.getCurrentUser().getPhoneNumber().toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            IndividualModel model = snapshot.getValue(IndividualModel.class);
                            individualList.add(model);
                        }
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    class IndividualAdapter extends RecyclerView.Adapter<IndividualAdapter.IndividualViewHolder> {

        Context context;
        ArrayList<IndividualModel> list = new ArrayList<>();

        public IndividualAdapter(Context context, ArrayList<IndividualModel> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public IndividualViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_custom_layout,parent,false);

            return new IndividualViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IndividualViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.groupNameTextView.setText(list.get(position).getName());
            FirebaseFirestore.getInstance().collection("Users").document(list.get(position).getPhoneNumber())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            UserDetails userDetails = documentSnapshot.toObject(UserDetails.class);
                            Glide.with(context).load(userDetails.getProfileImageUri()).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            }).into(holder.dpImageView);
                        }
                    });

            holder.chatsLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),IndividualChatActivity.class);
                    intent.putExtra("groupName",holder.groupNameTextView.getText().toString());
                    intent.putExtra("chatPageId",list.get(position).getChatPageId().toString());
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class IndividualViewHolder extends RecyclerView.ViewHolder {

            ImageView dpImageView;
            TextView groupNameTextView,lastMassageTextView,chats_lastMassageTime;
            LinearLayout chatsLinearLayout;
            ProgressBar progressBar;

            public IndividualViewHolder(@NonNull View itemView) {
                super(itemView);
                dpImageView = (ImageView)itemView.findViewById(R.id.dpImageView);
                groupNameTextView = (TextView)itemView.findViewById(R.id.groupNameTextView);
                lastMassageTextView = (TextView)itemView.findViewById(R.id.lastMassageTextView);
                chatsLinearLayout = (LinearLayout)itemView.findViewById(R.id.chatLinearLayout);
                chats_lastMassageTime = (TextView)itemView.findViewById(R.id.chats_lastMassageTime);
                progressBar = (ProgressBar)itemView.findViewById(R.id.spin_kit);

                lastMassageTextView.setVisibility(View.GONE);
                chats_lastMassageTime.setVisibility(View.GONE);
            }
        }
    }

}

class IndividualModel {
    String name;
    String phoneNumber;
    String chatPageId;

    public IndividualModel() {
    }

    public IndividualModel(String name, String phoneNumber, String chatPageId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.chatPageId = chatPageId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatPageId() {
        return chatPageId;
    }

    public void setChatPageId(String chatPageId) {
        this.chatPageId = chatPageId;
    }
}

