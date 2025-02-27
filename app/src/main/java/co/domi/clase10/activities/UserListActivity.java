package co.domi.clase10.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.domi.clase10.R;
import co.domi.clase10.comm.Actions;
import co.domi.clase10.events.OnUserListListener;
import co.domi.clase10.lists.adapter.UserAdapter;
import co.domi.clase10.model.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity implements View.OnClickListener, UserAdapter.OnUserClickListener {

    private User myUser;
    private RecyclerView userList;
    private FirebaseFirestore db;
    private Button logoutBtn, profileBtn;
    private boolean isLoggingout = false;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        //Configuracion de la lista
        userList = findViewById(R.id.userList);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileBtn = findViewById(R.id.profileBtn);


        adapter = new UserAdapter();
        adapter.setListener(this);
        userList.setAdapter(adapter);
        userList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        userList.setLayoutManager(manager);


        //Recuperar el user de la actividad pasada
        myUser = (User) getIntent().getExtras().getSerializable("myUser");
        //Listar usuarios
        db = FirebaseFirestore.getInstance();



        //Habilitar los clicks a items de la lista
        logoutBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

    }


    public void loadUserList(){
        Query userReference = db.collection("users").orderBy("username").limit(10);
        userReference.get().addOnCompleteListener(
                task -> {
                    adapter.clear();
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        User user = doc.toObject(User.class);
                        adapter.addUser(user);
                    }
                }
        );
    }


    @Override
    protected void onPause() {

        if(isLoggingout){
            FirebaseMessaging.getInstance().unsubscribeFromTopic(myUser.getUsername());
        }else{
            //Suscribimos a nuestra propia rama
            FirebaseMessaging.getInstance().subscribeToTopic(myUser.getUsername());
        }


        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(myUser.getUsername());
        loadUserList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logoutBtn:
                isLoggingout = true;
                finish();
                break;
            case R.id.profileBtn:
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("myUser", this.myUser);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onUserClick(User userClicked) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("myUser",this.myUser);
        intent.putExtra("userClicked",userClicked);
        startActivity(intent);
    }
}