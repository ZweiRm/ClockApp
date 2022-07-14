package xin.ahza.clockapp.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xin.ahza.clockapp.R;

public class GlobalDialogSingle extends Dialog {
    private TextView globalDialogMessage;
    private TextView okBtn;
    private Context mContext;
    private String title;
    private String message;
    private String positiveText;
    private OnClickListener onClickListenerPositive;

    public GlobalDialogSingle(@NonNull Context context) {
        super(context);
    }

    public GlobalDialogSingle(@NonNull Context context, String title, String message, String positiveText, OnClickListener onClickListenerPositive) {
        super(context);

        this.mContext = context;
        this.title = title;
        this.message = message;
        this.positiveText = positiveText;
        this.onClickListenerPositive = onClickListenerPositive;
    }

    public GlobalDialogSingle(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.global_dialog);
        globalDialogMessage = findViewById(R.id.global_dialog_message);
        okBtn = findViewById(R.id.global_dialog_ok_btn);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        layoutParams.width = (int) (displayMetrics.widthPixels * 0.8);
        dialogWindow.setAttributes(layoutParams);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        GlobalDialogSingle.this.setCanceledOnTouchOutside(false);
        GlobalDialogSingle.this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        globalDialogMessage.setText(message);
        okBtn.setText(positiveText);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListenerPositive.onClick(GlobalDialogSingle.this, DialogInterface.BUTTON_POSITIVE);
            }
        });
    }
}
