package com.example.updateversion;

import com.example.updateversion.http.AppUpdateManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button btnCheckUpdate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnCheckUpdate = (Button) findViewById(R.id.btn_checkupdate);

		btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 这里去检查更新
				AppUpdateManager.getInstance(MainActivity.this).checkUpdate();

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (Utils.isAppOnForeground(this)) {
		AppUpdateManager.getInstance(this).appForeground();
		// }

	}

	@Override
	protected void onPause() {
		super.onPause();
		// if (!Utils.isAppOnForeground(this)) {
		AppUpdateManager.getInstance(this).appBackground();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
