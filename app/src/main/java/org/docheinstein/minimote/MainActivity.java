package org.docheinstein.minimote;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.controller.MinimoteControllerActivity;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.discovery.MinimoteDiscoveredServer;
import org.docheinstein.minimote.discovery.MinimoteServerDiscoverer;
import org.docheinstein.minimote.edit.EditServerActivity;
import org.docheinstein.minimote.utils.IpUtils;
import org.docheinstein.minimote.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements MinimoteServerDiscoverer.MinimoteServerDiscovererListener {
    private static final String TAG = "MainActivity";

    private ServerListAdapter uiServerListAdapter;
    private RecyclerView.LayoutManager uiServerListManager;
    private RecyclerView uiServerList;

    private View uiDiscoverProgressContainer;
    private ProgressBar uiDiscoverProgress;
    private ScheduledFuture mDiscoveryProgressUpdated;

    private MinimoteServerDiscoverer mDiscoverer;


    public static class AddServerFragment extends DialogFragment {
        static final String FRAGMENT_TAG = "add_server_fragment";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_server, null);
            final EditText uiServerAddress = dialogView.findViewById(R.id.uiAddServerAddressInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            return builder
                    .setTitle(R.string.add_server)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add_server_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String ipv4 = uiServerAddress.getText().toString();
                            Log.d(TAG, "Trying to add server with IP: " + ipv4);

                            if (!IpUtils.isValidIPv4(ipv4)) {
                                Log.w(TAG, "Invalid IPv4 address");
                                showInvalidAdditionAlert();
                                return;
                            }

                            // Add server
                            DB.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Valid IPv4, adding minimote server to DB");
                                    MinimoteServerEntity serverEntity = new MinimoteServerEntity(ipv4, null, null);
                                    DB.getInstance().minimoteServerDao().addOrReplace(serverEntity);
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.add_server_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "Aborting server addition");
                        }
                    })
                    .create();
        }

        private void showInvalidAdditionAlert() {
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(R.string.add_server_failed_dialog_ipv4_title)
                .setMessage(R.string.add_server_failed_dialog_ipv4_message)
                .setPositiveButton(R.string.ok, null)
                .show();
        }
    }

    private class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ServerViewHolder> {

        private List<MinimoteServerEntity> mServers;

        public void setServers(List<MinimoteServerEntity> servers) {
            Log.d(TAG, "Updating server list UI");
            mServers = servers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.server_list_item, parent, false);
            return new ServerViewHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
            if (mServers == null) {
                Log.w(TAG, "Null server list");
                return;
            }

            if (position < 0 || position >= mServers.size()) {
                // Fallback
                Log.w(TAG, "Invalid list position (" + position + ")");
                holder.uiServerAddress.setText("");
                holder.uiServerDisplayName.setText("");
                holder.mRowClickListener = null;
                return;
            }

            final MinimoteServerEntity server = mServers.get(position);

            holder.mRowClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "Clicked on row");
                    Intent i = new Intent(MainActivity.this, MinimoteControllerActivity.class);
                    i.putExtra(MinimoteControllerActivity.EXTRA_SERVER_ADDRESS, server.address);
                    MainActivity.this.startActivity(i);
                }
            };
            holder.uiServerAddress.setText(
                    server.address);
            holder.uiServerDisplayName.setText(
                    StringUtils.firstValid(server.displayName, server.hostname, server.address));
            holder.uiServerEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "Clicked on edit server button");
                    Intent i = new Intent(MainActivity.this, EditServerActivity.class);
                    i.putExtra(EditServerActivity.EXTRA_SERVER_ADDRESS, server.address);
                    MainActivity.this.startActivity(i);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mServers != null ? mServers.size() : 0;
        }

        private class ServerViewHolder extends RecyclerView.ViewHolder{
            ImageView uiServerEdit;
            TextView uiServerDisplayName;
            TextView uiServerAddress;
            View.OnClickListener mRowClickListener;

            ServerViewHolder(@NonNull View itemView) {
                super(itemView);
                uiServerEdit = itemView.findViewById(R.id.uiServerEdit);
                uiServerDisplayName = itemView.findViewById(R.id.uiServerDisplayName);
                uiServerAddress = itemView.findViewById(R.id.uiServerAddress);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRowClickListener != null)
                            mRowClickListener.onClick(v);
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Add server
        findViewById(R.id.uiAddServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServerButtonClick();
            }
        });

        // Discover server
        findViewById(R.id.uiDiscoverServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDiscoverServerButtonClick();
            }
        });

        uiServerList = findViewById(R.id.uiServerList);
        uiServerList.setHasFixedSize(true);

        uiServerListManager = new LinearLayoutManager(this);
        uiServerList.setLayoutManager(uiServerListManager);

        uiServerListAdapter = new ServerListAdapter();
        uiServerList.setAdapter(uiServerListAdapter);

        uiDiscoverProgressContainer = findViewById(R.id.uiDiscoverProgressContainer);
        uiDiscoverProgress = findViewById(R.id.uiDiscoverProgress);

        findViewById(R.id.uiDiscoverStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Stop discovery required by the user");
                stopDiscovery();
            }
        });

        DB.init(this);

        DB.getInstance().minimoteServerDao().getAllObservable().observe(
                MainActivity.this, new Observer<List<MinimoteServerEntity>>() {
            @Override
            public void onChanged(List<MinimoteServerEntity> servers) {
                Log.v(TAG, "onChanged() for minimote servers");

                if (servers == null || servers.isEmpty()) {
                    Log.d(TAG, "No servers yet");
                } else {
                    StringBuilder sb = new StringBuilder("\n");
                    for (MinimoteServerEntity server : servers) {
                        sb.append(">>: ").append(server).append("\n");
                    }
                    Log.i(TAG, sb.toString());
                }

                uiServerListAdapter.setServers(servers);
            }
        });
    }


    @Override
    public void onServerDiscovered(final MinimoteDiscoveredServer server) {
        Log.i(TAG, "Discovered server: " + server);

        // Add server (if necessary)
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                MinimoteServerEntity serverEntity =
                        DB.getInstance().minimoteServerDao().getByAddress(server.getAddress());

                if (serverEntity != null) {
                    Log.d(TAG, "Discovered server already exists, updating it...");
                    serverEntity.hostname = server.getHostname();
                } else {
                    Log.d(TAG, "Discovered server does not exists, adding it");
                    serverEntity = new MinimoteServerEntity(server.getAddress(), server.getHostname(), null);
                }

                DB.getInstance().minimoteServerDao().addOrReplace(serverEntity);
            }
        });
    }

    @Override
    public void onDiscoveryStarted() {
        Log.i(TAG, "Discovery STARTED");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiDiscoverProgressContainer.setVisibility(View.VISIBLE);
                uiDiscoverProgress.setProgress(0);
            }
        });

        long percentagePeriod = Conf.DISCOVERY_TIMEOUT_MS / 100;

        if (mDiscoveryProgressUpdated != null) {
            Log.v(TAG, "Interrupting progress bar updater");
            mDiscoveryProgressUpdated.cancel(true);
        }

        mDiscoveryProgressUpdated = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uiDiscoverProgress.incrementProgressBy(1);
                    }
                });
            }
        }, 0, percentagePeriod, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDiscoveryFinished() {
        Log.i(TAG, "Discovery FINISHED");

        mDiscoveryProgressUpdated.cancel(true);
        mDiscoveryProgressUpdated = null;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiDiscoverProgressContainer.setVisibility(View.GONE);
            }
        });
    }

    private void handleAddServerButtonClick() {
        Log.v(TAG, "Clicked add server button");
        AddServerFragment addServerFragment = new AddServerFragment();
        addServerFragment.show(getSupportFragmentManager(), AddServerFragment.FRAGMENT_TAG);
    }

    private void handleDiscoverServerButtonClick() {
        Log.v(TAG, "Clicked discoverAndWait server button");

        // Ask confirmation

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v(TAG, "User confirmed discovery, starting it...");
                startDiscovery();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.w(TAG, "Not performing discovery, cancelled by the user");
            }
        });
        builder.setTitle(R.string.start_discovery_dialog_title);
//        builder.setMessage(R.string.start_discovery_dialog_message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private synchronized void startDiscovery() {
        Log.v(TAG, "MainActivity.startDiscovery()");


        new Thread(new Runnable() {
            @Override
            public void run() {
                mDiscoverer = new MinimoteServerDiscoverer(MainActivity.this, Conf.UDP_PORT);
                mDiscoverer.discoverAndWait(Conf.DISCOVERY_TIMEOUT_MS);
            }
        }).start();
    }

    private synchronized void stopDiscovery() {
        Log.v(TAG, "MainActivity.stopDiscovery()");

        mDiscoverer.abortDiscovery();
    }
}
