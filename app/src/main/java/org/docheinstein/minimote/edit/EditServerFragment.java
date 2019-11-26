package org.docheinstein.minimote.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.utils.StringUtils;

public class EditServerFragment extends MinimoteFragment {

    private static final String TAG = "EditServerFragment";

    private TextView uiDisplayName;
    private TextView uiAddress;
    private TextView uiHostname;

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

        showBackButton();

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

        uiDisplayName = view.findViewById(R.id.uiServerDisplayNameEdit);
        uiAddress = view.findViewById(R.id.uiServerAddressView);
        uiHostname = view.findViewById(R.id.uiServerHostnameView);


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

                ui(new Runnable() {
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
                        uiHostname.getText().toString(),
                        uiDisplayName.getText().toString()
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
                        .deleteByAddress(uiAddress.getText().toString()) > 0) {
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
