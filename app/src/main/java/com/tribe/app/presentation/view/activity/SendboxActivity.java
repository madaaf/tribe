package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import timber.log.Timber;

/**
 * Created by madaaflak on 07/06/2017.
 */

public class SendboxActivity extends Activity {

  private static String PASS = "1987";

  @BindView(R.id.sandboxId) EditText sendboxEditText;

  // VARIABLES
  private Unbinder unbinder;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sandbox);
    unbinder = ButterKnife.bind(this);
    setFinishOnTouchOutside(false);
  }

  @OnClick(R.id.btnEnable) void enableSendboxAction() {
    if (sendboxEditText.getText().toString().equals(PASS)) {
     // Digits.enableSandbox();
      Toast toast = Toast.makeText(getApplicationContext(), "enable Sandbox", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    } else

    {
      Toast toast = Toast.makeText(getApplicationContext(), "wrong pass", Toast.LENGTH_SHORT);
      toast.show();
    }
  }

  @OnClick(R.id.btnDisable) void disableSendboxAction() {
   // Digits.disableSandbox();
    Timber.w("diable sendbox");
    Toast toast = Toast.makeText(getApplicationContext(), "disable Sandbox", Toast.LENGTH_SHORT);
    toast.show();
    finish();
  }

  @OnClick(R.id.btnCancel) void btnCancelAction() {
    finish();
  }
}
