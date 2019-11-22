package org.docheinstein.minimote.edit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.utils.StringUtils;

public class EditServerActivity extends AppCompatActivity {

    private static final String TAG = "EditServerActivity";

    public static final String EXTRA_SERVER_ADDRESS = "server_address";

    private TextView uiDisplayName;
    private TextView uiAddress;
    private TextView uiHostname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_server);

        Intent i = getIntent();

        if (i == null) {
            Log.e(TAG, "Invalid intent?");
            finish();
            return;
        }

        final String serverAddress = i.getStringExtra(EXTRA_SERVER_ADDRESS);

        if (!StringUtils.isValid(serverAddress)) {
            Log.e(TAG, "Invalid server address provided to EditServerActivity");
            finish();
            return;
        }

        uiDisplayName = findViewById(R.id.uiServerDisplayNameEdit);
        uiAddress = findViewById(R.id.uiServerAddressView);
        uiHostname = findViewById(R.id.uiServerHostnameView);


        // Add server (if necessary)
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final MinimoteServerEntity serverEntity =
                        DB.getInstance().minimoteServerDao().getByAddress(serverAddress);

                if (serverEntity == null) {
                    Log.e(TAG, "Cannot find server with address: " + serverAddress);
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Retrieved server details, updating UI accordingly");
                        uiDisplayName.setText(serverEntity.displayName);
                        uiAddress.setText(serverEntity.address);
                        uiHostname.setText(serverEntity.hostname);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_server_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.uiSaveServerMenuItem:
                handleSaveButtonClick();
                return true;
            case R.id.uiDeleteServerMenuItem:
                handleDeleteButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleSaveButtonClick() {
        Log.v(TAG, "Clicked on save button");
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                MinimoteServerEntity serverEntity = new MinimoteServerEntity(
                        uiAddress.getText().toString(),
                        uiHostname.getText().toString(),
                        uiDisplayName.getText().toString()
                );
                Log.i(TAG, "Updating server info to: " + serverEntity);

                DB.getInstance().minimoteServerDao().addOrReplace(serverEntity);
                finish();
            }
        });
    }

    private void handleDeleteButtonClick() {
        Log.v(TAG, "Clicked on delete button");
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                String addr = uiAddress.getText().toString();
                if (DB.getInstance().minimoteServerDao()
                        .deleteByAddress(uiAddress.getText().toString()) > 0) {
                    Log.i(TAG, "Server " + addr + " has been deleted");
                } else {
                    Log.w(TAG, "Failed deletion of server " + addr);
                }

                finish();
            }
        });
    }
}
