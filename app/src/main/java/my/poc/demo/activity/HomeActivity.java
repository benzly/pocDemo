package my.poc.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.greendao.MessageDialogue;

import my.poc.demo.R;
import my.poc.demo.activity.func.AddChannelFunc;
import my.poc.demo.fragment.ChannelListFragment;
import my.poc.demo.fragment.ContactsFragment;
import my.poc.demo.fragment.MessageListFragment;
import my.poc.demo.fragment.OtherFragment;
import my.poc.demo.fragment.PttFragment;
import my.poc.demo.widget.InterceptViewPager;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView navTitle;
    TextView navSubTitle;
    InterceptViewPager viewPager;
    SectionsPagerAdapter sectionsPagerAdapter;
    MessageListFragment messageListFragment;
    ChannelListFragment chanelListFragment;
    PttFragment pttFragment;
    ContactsFragment contactsFragment;
    OtherFragment otherFragment;
    PocEngine pocEngine = PocEngineFactory.get();
    Handler handler = new Handler();

    //===================
    AddChannelFunc addChannelFunc;
    //===================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setup();
    }

    private void setup() {
        addChannelFunc = new AddChannelFunc(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (InterceptViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(4);

        navTitle = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_title);
        navSubTitle = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_sub_title);

        if (pocEngine.getCurrentUser() != null) {
            navTitle.setText(pocEngine.getCurrentUser().getName());
        }
        int state = pocEngine.getServiceState();
        switch (state) {
            case IPocEngineEventHandler.ServiceState.STATE_CONNECTED:
                navSubTitle.setText("服务已连接");
                break;
            case IPocEngineEventHandler.ServiceState.STATE_CONNECTING:
                navSubTitle.setText("服务连接中......");
                break;
            case IPocEngineEventHandler.ServiceState.STATE_DISCONNECTED:
                navSubTitle.setText("服务断开连接");
                break;
        }
        pocEngine.addEventHandler(pocEngineEventHandler);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_ptt:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_message:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_contacts:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_channel_list:
                    viewPager.setCurrentItem(3);
                    return true;
                case R.id.navigation_other:
                    viewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }
    };

    private IPocEngineEventHandler pocEngineEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onServiceConnected() {
            navSubTitle.setText("服务已连接");
        }

        @Override
        public void onServiceConnecting() {
            navSubTitle.setText("服务连接中......");
        }

        @Override
        public void onServiceConnectFailed(int reason) {
            navSubTitle.setText("服务连接失败" + reason);
        }

        @Override
        public void onServiceDisconnected(int reason) {
            navSubTitle.setText("服务断开连接,reason: " + reason);
        }

        @Override
        public void onNewConversationCreated(MessageDialogue messageDialogue) {
            if (messageListFragment != null) {
                messageListFragment.addNewConversation(messageDialogue);
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_channel) {
            addChannelFunc.start();
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
        } else if (id == R.id.nav_gallery) {
        } else if (id == R.id.nav_slideshow) {
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "退出", Toast.LENGTH_SHORT).show();
            pocEngine.logout();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    HomeActivity.this.finish();
                }
            }, 500);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (pttFragment == null) {
                    pttFragment = PttFragment.newInstance();
                }
                return pttFragment;
            } else if (position == 1) {
                if (messageListFragment == null) {
                    messageListFragment = new MessageListFragment();
                }
                return messageListFragment;
            } else if (position == 2) {
                if (contactsFragment == null) {
                    contactsFragment = ContactsFragment.newInstance();
                }
                contactsFragment.bindViewPager(viewPager);
                return contactsFragment;
            } else if (position == 3) {
                if (chanelListFragment == null) {
                    chanelListFragment = ChannelListFragment.newInstance();
                }
                chanelListFragment.bindViewPager(viewPager);
                return chanelListFragment;
            } else if (position == 4) {
                if (otherFragment == null) {
                    otherFragment = OtherFragment.newInstance();
                }
                return otherFragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
