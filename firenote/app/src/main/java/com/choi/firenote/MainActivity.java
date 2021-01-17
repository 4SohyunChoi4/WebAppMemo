package com.choi.firenote;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //private AppBarConfiguration mAppBarConfiguration;
    private  FirebaseUser mFirebaseUser;
    private EditText etContent;
    private static FirebaseDatabase mFirebaseDatabase;
    private TextView txtName, txtEmail;
    private NavigationView mNavigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private String selectedMemoKey;
    private FirebaseAuth mFirebaseAuth;

    static {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        etContent = findViewById(R.id.content);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseUser = mFirebaseAuth.getCurrentUser(); //-> 인증x이면 null값 들어옴
        if( mFirebaseUser == null){
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return ;
        }
        setSupportActionBar(toolbar);
        findViewById(R.id.new_memo).setOnClickListener(new View.OnClickListener() {
            @Override//버튼 클릭했을 때 신규 메모 쓰도록
            public void onClick(View view) {
                initMemo();
            }
        });
        findViewById(R.id.save_memo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( selectedMemoKey == null )
                    saveMemo();
                else
                    updateMemo();
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        View headerView = mNavigationView.getHeaderView(0);
        txtName = headerView.findViewById(R.id.txtName);
        txtEmail = headerView.findViewById(R.id.txtEmail);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home).setDrawerLayout(drawer).build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);

        profileUpdate();
        displayMemos();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Memo selectedMemo = (Memo) item.getActionView().getTag();
                etContent.setText(selectedMemo.getTxt()); //에딧텍스트 창에다가 불러온 글 놓음
                selectedMemoKey = selectedMemo.getKey(); //나중에 수정용으로 저장해놓는 키값
                DrawerLayout drawer = findViewById(R.id. drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //옵션메뉴
        int id = item.getItemId();
        if(id == R.id.action_delete)
            deleteMemo();
        else if ( id == R.id.action_logout)
            logout();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initMemo() {
        selectedMemoKey=null;
        etContent.setText("");
    }

    private void logout(){
        Snackbar.make(etContent,"로그아웃하시겠습니까?",Snackbar.LENGTH_LONG).setAction("로그아웃", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(etContent,"로그아웃되었습니다",Snackbar.LENGTH_SHORT).show();
                mFirebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                finish();
            }
        }).show();
    }

    private void saveMemo() {
        String text = etContent.getText().toString();
        if(text.isEmpty())
            return;
        Memo memo = new Memo();
        memo.setTxt(text);
        memo.setCreateDate(new Date().getTime());
        mFirebaseDatabase.getReference("memos/"+mFirebaseUser.getUid())
                .push()
                .setValue(memo)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(etContent, "메모가 저장되었습니다", Snackbar.LENGTH_SHORT).show();
                        initMemo();
                    }
                });
    }
    private void deleteMemo(){
        if (selectedMemoKey == null)
            return;
        Toast.makeText(getApplicationContext(), "ㅅㅂ", Toast.LENGTH_SHORT).show();
        Snackbar.make(etContent,"메모를 삭제하시겠습니까?",Snackbar.LENGTH_SHORT).setAction("삭제", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDatabase
                        .getReference("memos/"+mFirebaseUser.getUid()+"/"+selectedMemoKey)
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Snackbar.make(etContent,"메모가 삭제되었습니다",Snackbar.LENGTH_SHORT).show();

                            }
                        });//firebase에서 응답을 주는 서버 함수 구현
            }
        }).show();


    }

    private void updateMemo(){
        String text = etContent.getText().toString();
        if (text.isEmpty()){
            return ;
        }
        Memo memo = new Memo();
        memo.setTxt(text);
        memo.setCreateDate(new Date().getTime());
        mFirebaseDatabase
                .getReference("memos/"+mFirebaseUser.getUid()+ "/" + selectedMemoKey)
                .setValue(memo) //push를 넣으면 또 '추가'가 됨
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(etContent,"메모가 수정되었습니다",Snackbar.LENGTH_SHORT).show();
                        selectedMemoKey=null;
                    }
                });

    }
    private void profileUpdate(){
        txtEmail.setText(mFirebaseUser.getEmail());
        txtName.setText(mFirebaseUser.getDisplayName());
    }

    private void displayMemos(){
        mFirebaseDatabase.getReference("memos/"+mFirebaseUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Memo memo = snapshot.getValue(Memo.class);
                        memo.setKey(snapshot.getKey());
                        displayMemoList(memo);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {//데이터 수정이 일어났을 때
                        Memo memo = snapshot.getValue(Memo.class);
                        memo.setKey(snapshot.getKey()); //데이터를 받아온다
                        for(int i=0 ; i<mNavigationView.getMenu().size() ; i++) {
                            MenuItem menuItem = mNavigationView.getMenu().getItem(i);
                            if (memo.getKey().equals(((Memo)menuItem.getActionView().getTag()).getKey())){//tag -> Memo로
                                menuItem.getActionView().setTag(memo);
                                menuItem.setTitle(memo.getTxt());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void displayMemoList(Memo memo){
        Menu leftMenu = mNavigationView.getMenu(); //네비게이션 바 변수
        //MenuItem item = leftMenu.add(memo.getTitle()); //에다가 title을 단 리스트 추가함
        MenuItem item = leftMenu.add(memo.getTxt());
        View view = new View(getApplication());
        view.setTag(memo); //view에다가 memo의 내용값 지정
        item.setActionView(view);
    }


}