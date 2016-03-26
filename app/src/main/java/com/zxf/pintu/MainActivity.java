package com.zxf.pintu;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.zxf.pintu.view.PintuGameLayout;

public class MainActivity extends AppCompatActivity implements PintuGameLayout.OnSuccessListener {
  private PintuGameLayout mGameLayout;
  private AlertDialog.Builder mAlterDialog;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mGameLayout = (PintuGameLayout) findViewById(R.id.game_layout);
    mGameLayout.setOnSuccessListener(this);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_restart:
        mGameLayout.restartGame();
        break;
      case R.id.action_type:
        mGameLayout.changeType();
        break;
      case R.id.action_exit:
        this.finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onSucess() {
    if (mAlterDialog == null) mAlterDialog = new AlertDialog.Builder(this);
    mAlterDialog.setTitle("恭喜通过");
    mAlterDialog.setMessage("立即挑战下一关?");
    mAlterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        mGameLayout.restartGame();
        dialog.dismiss();
      }
    });
    mAlterDialog.setPositiveButton("立即挑战", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        mGameLayout.nextLevel();
        dialog.dismiss();
      }
    });
    mAlterDialog.setCancelable(false);
    mAlterDialog.show();
  }
}
