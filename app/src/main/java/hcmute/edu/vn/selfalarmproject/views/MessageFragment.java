package hcmute.edu.vn.selfalarmproject.views;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.adapters.MessageAdapter;
import hcmute.edu.vn.selfalarmproject.models.MessageModel;
import hcmute.edu.vn.selfalarmproject.utils.SharedPreferencesHelper;

public class MessageFragment extends Fragment {
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private FloatingActionButton fabAddMessage;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private List<MessageModel> messages;

    public MessageFragment() {
        super(R.layout.fragment_message);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        fabAddMessage = view.findViewById(R.id.fabAddMessage);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(messageAdapter);
        String googleUid = SharedPreferencesHelper.getGoogleUid(getContext());
        Log.d("MyApp", "Google UID: " + googleUid);
        databaseReference = FirebaseDatabase.getInstance("https://week6-8ecb2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference(googleUid);
        fetchMessagesFromFirebase();
        setupSwipeToDelete();
        fabAddMessage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewMessageActivity.class);
            startActivity(intent);

        });

    }

    private void fetchMessagesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                HashMap<String, MessageModel> latestMessagesMap = new HashMap<>();


                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    MessageModel message = messageSnapshot.getValue(MessageModel.class);

                    if (message != null) {
                        if (!latestMessagesMap.containsKey(message.getId()) ||
                                (message.getTime() != null && latestMessagesMap.get(message.getId()).getTime() != null &&
                                        message.getTime().compareTo(latestMessagesMap.get(message.getId()).getTime()) > 0) || !message.getSender().equals("Tôi")) {
                            latestMessagesMap.put(message.getId(), message);
                        }
                    }
                }

                messages.addAll(latestMessagesMap.values());

                Collections.sort(messages, (m1, m2) -> {
                    if (m1.getTime() == null || m2.getTime() == null) return 0;
                    return m2.getTime().compareTo(m1.getTime());
                });

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi tải dữ liệu", error.toException());
                Toast.makeText(getContext(), "Lỗi khi tải tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                messageAdapter.removeMessage(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_delete);
                ColorDrawable background = new ColorDrawable(Color.RED);

                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                if (dX > 0) {
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds(itemView.getLeft(), itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                } else if (dX < 0) {
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    background.setBounds((int) (itemView.getRight() + dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
                icon.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

}