package my.poc.demo.activity.func;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;
import com.huamai.poc.greendao.Channel;
import com.huamai.poc.greendao.User;

import java.util.List;

import my.poc.demo.widget.MultipleSelectDialog;

public class AddChannelFunc {

    Context context;
    List<User> users;
    String channelName;

    public AddChannelFunc(Context context) {
        this.context = context;
    }

    public void start() {
        final MultipleSelectDialog msd = new MultipleSelectDialog(context, PocEngineFactory.get().getContactsUserList());
        msd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                users = msd.getSelects();
                showInputNameDialog();
            }
        });
        msd.show();
    }

    private void showInputNameDialog() {
        if (users == null || users.size() == 0) {
            return;
        }

        final EditText et = new EditText(context);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        new AlertDialog.Builder(context).setTitle("输入对讲组名称")
                .setIcon(android.R.drawable.ic_dialog_info).setView(et)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = et.getText().toString();
                        if (!"".equals(name)) {
                            channelName = name;
                            create();
                        }
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void create() {
        PocEngineFactory.get().createAudioPTTChannel(channelName, users, new IPocEngineEventHandler.Callback<Channel>() {
            @Override
            public void onResponse(final Channel channel) {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, channel != null ? "成功" : "失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
