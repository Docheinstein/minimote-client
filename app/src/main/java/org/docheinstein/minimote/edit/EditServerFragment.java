package org.docheinstein.minimote.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.utils.IntUtils;
import org.docheinstein.minimote.utils.NetUtils;
import org.docheinstein.minimote.utils.StringUtils;

public class EditServerFragment extends MinimoteFragment {

    private static final String TAG = "EditServerFragment";

    private TextView uiDisplayName;
    private TextView uiAddress;
    private TextView uiPort;
    private TextView uiHostname;
    private Switch uiAutoConnect;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_server, container, false);

        Bundle args = getArguments();

        if (args == null) {
            Log.e(TAG, "Invalid args?");
            return null;
        }

        final String serverAddress = EditServerFragmentArgs.fromBundle(args).getServerAddress();

        if (!StringUtils.isValid(serverAddress)) {
            Log.e(TAG, "Invalid server address provided to EditServerFragment");
            return null;
        }

        final int serverPort = EditServerFragmentArgs.fromBundle(args).getServerPort();

        uiDisplayName = view.findViewById(R.id.uiServerDisplayNameEdit);
        uiAddress = view.findViewById(R.id.uiServerAddressView);
        uiPort = view.findViewById(R.id.uiServerPortView);
        uiHostname = view.findViewById(R.id.uiServerHostnameView);
        uiAutoConnect = view.findViewById(R.id.uiAutoConnect);


        // Add server (if necessary)
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final MinimoteServerEntity serverEntity =
                        DB.getInstance().minimoteServerDao().get(serverAddress, serverPort);

                if (serverEntity == null) {
                    Log.e(TAG, "Cannot find server with address: " + serverAddress + ":" + serverPort);
                    return;
                }

                ui(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Retrieved server details, updating UI accordingly");
                        uiDisplayName.setText(serverEntity.displayName);
                        uiAddress.setText(serverEntity.address);
                        uiPort.setText(String.valueOf(serverEntity.port));
                        uiHostname.setText(serverEntity.hostname);
                        uiAutoConnect.setChecked(serverEntity.autoConnect);
                    }
                });
            }
        });

        setToolbarTitle("Edit " + serverAddress);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.edit_server_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
                        IntUtils.parseString(uiPort.getText().toString()),
                        uiHostname.getText().toString(),
                        uiDisplayName.getText().toString(),
                        uiAutoConnect.isChecked()

                );
                Log.i(TAG, "Updating server info to: " + serverEntity);

                DB.getInstance().minimoteServerDao().addOrReplace(serverEntity);

                ui(new Runnable() {
                    @Override
                    public void run() {
                        goBack();
                    }
                });
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
                        .delete(uiAddress.getText().toString(),
                                IntUtils.parseString(uiPort.getText().toString(), Conf.DEFAULT_PORT)) > 0) {
                    Log.i(TAG, "Server " + addr + " has been deleted");
                } else {
                    Log.w(TAG, "Failed deletion of server " + addr);
                }

                ui(new Runnable() {
                    @Override
                    public void run() {
                        goBack();
                    }
                });
            }
        });
    }
}
