package com.example.zyq.kaminotetest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import org.litepal.crud.DataSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyq on 2018/3/6.
 * 项目名称：Kami Note
 * MainActivity
 */

public class MainActivity extends AppCompatActivity {
    private static final String ACTIVITY_TAG = "MainActivity";  //打印日志的TAG
    List<MyNote> mNoteTemp = new ArrayList<>();

    //private:
    private DrawerLayout mDrawerLayout; //滑动菜单
    private TextView tv_noMore;  //没有更多内容的文本
    private long mExitTime = 0; //记录点击返回按钮的时间

    //public:
    public static List<MyNote> mNote;   //保存note的列表
    public static int notePosition; //记录笔记位置
    public RecyclerView noteListView;   //RecyclerView 的note 列表
    public static int longClickPosition = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    noteListView.setVisibility(View.INVISIBLE);
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    noteListView.setVisibility(View.VISIBLE);
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    //当App启动
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_noMore = findViewById(R.id.no_more); //没有更多内容
        mDrawerLayout = findViewById(R.id.drawer_layout);   //滑动菜单
        noteListView = findViewById(R.id.note_list);    //note列表

        //设置toolbar的左侧菜单为显示状态
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//        }

        //从数据库中读取存在的笔记，临时保存
        mNoteTemp = DataSupport.findAll(MyNote.class);
        //判断是否读取到了数据
        if (mNoteTemp.size() != 0) {
            //如果成功读取了数据，则以mNote正式保存下来
            mNote = mNoteTemp;
            refreshNoteListView(noteListView);  //刷新
        } else {
            //如果没有读取到，就将mNote设置为新的List，以备保存
            mNote = new ArrayList<>();
            //将"没有更多内容"从布局显示
            tv_noMore.setVisibility(View.VISIBLE);
        }
        mNoteTemp = null;   //将temp指向设为空
    }

    //回到MainActivity时刷新RecyclerView
    @Override
    protected void onResume() {
        super.onResume();
        if (mNote.size() != 0) {
            //如果已有数据且"没有更多内容"仍为显示状态，就把它隐藏掉
            tv_noMore = findViewById(R.id.no_more);
            if (tv_noMore.getVisibility() == View.VISIBLE) {
                tv_noMore.setVisibility(View.GONE);
            }
            noteListView = findViewById(R.id.note_list);
            refreshNoteListView(noteListView);
            //滑动到最后编辑的内容（太复杂，需简化）
            noteListView.scrollToPosition(notePosition == 0 ? mNote.size() : notePosition);
        }
    }

    //刷新RecyclerView，有待优化
    public void refreshNoteListView(RecyclerView recyclerView) {
        if (recyclerView != null) {
//            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));  //添加分割线
            LinearLayoutManager layoutManager = new LinearLayoutManager(this); //???
            layoutManager.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
            layoutManager.setReverseLayout(true);//列表翻转
            recyclerView.setLayoutManager(layoutManager);
            NoteAdapter2 adapter = new NoteAdapter2(mNote, MainActivity.this);
            recyclerView.setAdapter(adapter);
            noteListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add(0,666,0,"删除");
                }
            });
            if (mNote.size() != 0) {
                //如果存在数据且"没有更多内容"还在，则设置为GONE
                if (tv_noMore.getVisibility() == View.VISIBLE) {
                    tv_noMore.setVisibility(View.GONE);
                }
            } else {
                if (tv_noMore.getVisibility() != View.VISIBLE) {
                    tv_noMore.setVisibility(View.VISIBLE);
                }
            }

        } else {
            Log.d(ACTIVITY_TAG, "refreshNoteListView: 传入了空的recyclerView参数");
            MyToast.makeText(this, "发生错误", Toast.LENGTH_SHORT).show();
        }
    }

    //应用toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //点击toolbar的内容时启用的操作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.delete_all:   //点击了垃圾桶时
//                //AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//                //如果笔记条数已经为0，则提示没有笔记
//                if (mNote.size() == 0) {
////                    dialog.setTitle("提示");
////                    dialog.setMessage("已经没有笔记了，如果列表没有刷新，请重启程序。");
//                    AlertDialog.Builder dialog = buildAlertDialog(MainActivity.this,
//                            "提示",
//                            "已经没有笔记了，如果列表没有刷新，请重启程序。");
//                    dialog.setPositiveButton("重启", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    });
//                    dialog.setNegativeButton("取消", null);
//                    dialog.show();
//                } else {
//                    AlertDialog.Builder dialog = buildAlertDialog(MainActivity.this,
//                            "提示", "这是一项测试功能，是否删除全部内容？");
//                    dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            DataSupport.deleteAll(MyNote.class);    //删除所有note
//                            mNote = DataSupport.findAll(MyNote.class);  //重置mNote（可能可以省略）
//                            refreshNoteListView(noteListView);
//                            MyToast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    dialog.setNegativeButton("否", null);
//                    dialog.show();  //显示dialog
//                }
//                break;
            case R.id.add:    //点击了添加
                Intent jumpToCreateNote = new Intent(MainActivity.this, CreateNote.class);
                startActivity(jumpToCreateNote);
                break;
            case android.R.id.home: //点击左上角菜单键来启动滑动菜单
//                mDrawerLayout.openDrawer(GravityCompat.START);
//                break;

//            以下注释的代码尚未完成
            case R.id.user_name:
            case R.id.useLogo:

                break;

            case R.id.nav_call:
//                if (mNote.size() != 0) {
//                    Collections.sort(mNote, new Comparator<MyNote>() {
//                        @Override
//                        public int compare(MyNote o1, MyNote o2) {
//                            int i = o1.getTitle().compareTo(o2.getTitle());
//                            if (i == 0) {
//                                return o1.getContent().compareTo(o2.getContent());
//                            }
//                            return i;
//                        }
//                    });
//                    refreshNoteListView(noteListView);
//                }
                break;

        }
        return true;
    }

    //点击返回按钮的操作（"再按一次退出程序"）
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                MyToast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                finish();
            }
            return true;
//            作者：Carson_Ho
//            链接：https://www.jianshu.com/p/3dab35223b79
//            來源：简书
//            著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
        }
        return super.onKeyDown(keyCode, event);
    }


    public static AlertDialog.Builder buildAlertDialog(Context context, String alertTitle,
                                                String alertMessage) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(alertTitle);
        dialog.setMessage(alertMessage);
        return dialog;
    }

    //当点击长按菜单时要做的东西
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 666:   //点击了长按后出现的删除键时
                mNote.get(longClickPosition).delete();
                mNote.remove(longClickPosition);
                refreshNoteListView(noteListView);
                break;
        }
        return super.onContextItemSelected(item);
    }
}

