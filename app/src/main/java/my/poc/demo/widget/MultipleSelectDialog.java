package my.poc.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huamai.poc.greendao.User;
import com.unionbroad.app.util.Cn2Spell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.poc.demo.R;


public class MultipleSelectDialog extends Dialog {

    private final Context mContext;
    private EditText mSearchEt;
    private TextView title;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    public MultipleSelectDialog(Context context, List<User> users) {
        super(context, R.style.dialog);
        mContext = context;
        initView();
        initListView(users, null);
    }

    public MultipleSelectDialog(Context context, List<User> users, List<User> selected) {
        super(context, R.style.dialog);
        mContext = context;
        initView();
        initListView(users, selected);
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.dialog_multiple_select_layout, null);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.lv);
        mSearchEt = (EditText) contentView.findViewById(R.id.search_et);
        title = (TextView) contentView.findViewById(R.id.title);
        setContentView(contentView);

        mSearchEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSearchEt.hasFocus() && mAdapter != null) {
                    mAdapter.search(mSearchEt.getText().toString());
                }
            }
        });

        contentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultipleSelectDialog.this.dismiss();
            }
        });
        contentView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultipleSelectDialog.this.dismiss();
            }
        });
    }

    private void initListView(List<User> users, List<User> selects) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyAdapter(mContext, users, selects);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setCustomTitle(String msg) {
        title.setText(msg);
        title.setVisibility(View.VISIBLE);
    }

    public List<User> getSelects() {
        return mAdapter.getSelectItems();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        setHeight();
    }

    private void setHeight() {
        Window window = getWindow();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (window.getDecorView().getHeight() >= (int) (displayMetrics.heightPixels * 0.6)) {
            attributes.height = (int) (displayMetrics.heightPixels * 0.7);
        }
        window.setAttributes(attributes);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView userIcon;
        CheckBox checkbox;
        View itemRootView;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            userIcon = (ImageView) itemView.findViewById(R.id.user_icon);
            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);
            this.itemRootView = itemView;
            this.itemRootView.setFocusable(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkbox.performClick();
                }
            });
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        LayoutInflater inflater;
        String searchKey = "";
        ArrayList<User> dataList = new ArrayList<User>();
        ArrayList<User> filterUsers = new ArrayList<User>();
        Map<Long, User> hasSelects = new HashMap<Long, User>();
        Handler handler = new Handler();

        public MyAdapter(Context context, List<User> users, List<User> selects) {
            this.dataList.addAll(users);
            this.filterUsers.addAll(dataList);
            if (selects != null) {
                for (User select : selects) {
                    hasSelects.put(select.getNumber(), select);
                }
            }
            inflater = LayoutInflater.from(context);
        }

        final Runnable sortTask = new Runnable() {

            @Override
            public void run() {
                filterUsers.clear();
                for (User user : dataList) {
                    if (user.getName().contains(searchKey) || (user.getNumber() + "").contains(searchKey)
                            || Cn2Spell.getInstance().getSelling(user.getName()).contains(searchKey)) {
                        filterUsers.add(user);
                    }
                }
                notifyDataSetChanged();
            }
        };

        public void reset() {
            filterUsers.clear();
            filterUsers.addAll(dataList);
            notifyDataSetChanged();
        }

        public void search(String text) {
            handler.removeCallbacks(sortTask);
            if (!TextUtils.isEmpty(text)) {
                searchKey = text.trim();
                handler.postDelayed(sortTask, 500);
            } else {
                reset();
            }
        }

        public List<User> getSelectItems() {
            List<User> selects = new ArrayList<User>();
            for (Map.Entry<Long, User> entry : hasSelects.entrySet()) {
                selects.add(entry.getValue());
            }
            return selects;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(inflater.inflate(R.layout.item_user, null));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {
            final User user = filterUsers.get(position);
            viewHolder.name.setText(user.getName() + "");

            if (user.getStatus() == 1) {
                viewHolder.userIcon.setImageResource(R.drawable.ic_user_offline);
            } else {
                viewHolder.userIcon.setImageResource(R.drawable.ic_users_offline);
            }

            viewHolder.checkbox.setChecked(hasSelects.get(user.getNumber()) != null);
            viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean check = ((CheckBox) v).isChecked();
                    if (check) {
                        hasSelects.put(user.getNumber(), user);
                    } else {
                        hasSelects.remove(user.getNumber());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return filterUsers.size();
        }
    }
}