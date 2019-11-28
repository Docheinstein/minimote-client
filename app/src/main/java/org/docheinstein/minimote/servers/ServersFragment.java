package org.docheinstein.minimote.servers;

import android.app.Dialog;
import android.content.Context;
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
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.base.MinimoteFragment;
import org.docheinstein.minimote.commons.conf.Conf;
import org.docheinstein.minimote.controller.MinimoteControllerFragment;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.discovery.MinimoteDiscoveredServer;
import org.docheinstein.minimote.discovery.MinimoteServerDiscoverer;
import org.docheinstein.minimote.utils.IntUtils;
import org.docheinstein.minimote.utils.NetUtils;
import org.docheinstein.minimote.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServersFragment extends MinimoteFragment
        implements MinimoteServerDiscoverer.MinimoteServerDiscovererListener {
    private static final String TAG = "ServersFragment";

    private ServerListAdapter uiServerListAdapter;
    private RecyclerView.LayoutManager uiServerListManager;
    private RecyclerView uiServerList;

    private View uiDiscoverProgressContainer;
    private ProgressBar uiDiscoverProgress;
    private ScheduledFuture mDiscoveryProgressUpdater;

    private final Object mDiscovererLock = new Object();
    private MinimoteServerDiscoverer mDiscoverer;

    public static class AddServerFragment extends DialogFragment {
        static final String FRAGMENT_TAG = "add_server_fragment";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_server, null);
            final EditText uiServerAddress = dialogView.findViewById(R.id.uiAddServerAddress);
            final EditText uiServerPort = dialogView.findViewById(R.id.uiAddServerPort);

            uiServerPort.setText(String.valueOf(Conf.DEFAULT_PORT));

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            return builder
                    .setTitle(R.string.add_server)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add_server_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String serverAddress =
                                    uiServerAddress.getText().toString();
                            final Integer serverPort =
                                    IntUtils.parseString(uiServerPort.getText().toString(), Conf.DEFAULT_PORT);

                            Log.d(TAG, "Trying to add server with address: " + serverAddress + ":" + serverPort);

                            // Add server
                            DB.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Valid IPv4, adding minimote server to DB");
                                    MinimoteServerEntity serverEntity = new MinimoteServerEntity(
                                            serverAddress, serverPort, null, null, false);
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
                    ServersFragmentDirections.ActionController action =
                            ServersFragmentDirections.actionController(server.address, server.port);
                    Navigation.findNavController(v).navigate(action);
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
                    ServersFragmentDirections.ActionEditServer action =
                            ServersFragmentDirections.actionEditServer(server.address, server.port);
                    Navigation.findNavController(v).navigate(action);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.servers, container, false);


        // Add server
        view.findViewById(R.id.uiAddServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServerButtonClick();
            }
        });

        // Start discovery
        view.findViewById(R.id.uiDiscoverServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartDiscoveryButtonClick();
            }
        });

        // Stop discovery
        view.findViewById(R.id.uiDiscoverStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Stop discovery required by the user");
                handleStopDiscoveryButtonClick();
            }
        });

        uiServerList = view.findViewById(R.id.uiServerList);
        uiServerList.setHasFixedSize(true);

        uiServerListManager = new LinearLayoutManager(getContext());
        uiServerList.setLayoutManager(uiServerListManager);

        uiServerListAdapter = new ServerListAdapter();
        uiServerList.setAdapter(uiServerListAdapter);

        uiDiscoverProgressContainer = view.findViewById(R.id.uiDiscoverProgressContainer);
        uiDiscoverProgress = view.findViewById(R.id.uiDiscoverProgress);

        DB.getInstance().minimoteServerDao().getAllObservable().observe(
                ServersFragment.this, new Observer<List<MinimoteServerEntity>>() {
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

        setToolbarTitle("Servers");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v(TAG, "onActivityResult: requestCode: " + requestCode + " - resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MinimoteControllerFragment.RESULT_CONNECTIVITY) {
            if (resultCode == MinimoteControllerFragment.RESULT_CONNECTIVITY_ERROR) {
                Log.v(TAG, "MinimoteControllerFragment reported a connectivity error");
                Log.w(TAG, "Cannot establish connection with the server");
                showConnectionWithServerFailedAlert();
            } else {
                Log.v(TAG, "MinimoteControllerFragment finished gracefully");
            }
        }
    }

    @Override
    public void onServerDiscovered(final MinimoteDiscoveredServer server) {
        Log.i(TAG, "Discovered server: " + server);

        // Add server (if necessary)
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                MinimoteServerEntity serverEntity =
                        DB.getInstance().minimoteServerDao().get(server.getAddress(), server.getPort());

                if (serverEntity != null) {
                    Log.d(TAG, "Discovered server already exists, updating it...");
                    serverEntity.hostname = server.getHostname();
                } else {
                    Log.d(TAG, "Discovered server does not exists, adding it");
                    serverEntity = new MinimoteServerEntity(
                            server.getAddress(), server.getPort(),
                            server.getHostname(), null,
                            false
                    );
                }

                DB.getInstance().minimoteServerDao().addOrReplace(serverEntity);
            }
        });
    }

    @Override
    public void onDiscoveryStarted() {
        Log.i(TAG, "Discovery STARTED");

        ui(new Runnable() {
            @Override
            public void run() {
                uiDiscoverProgress.setProgress(0);
                uiDiscoverProgressContainer.setVisibility(View.VISIBLE);
            }
        });

        if (mDiscoveryProgressUpdater != null) {
            Log.v(TAG, "Interrupting progress bar updater");
            mDiscoveryProgressUpdater.cancel(true);
        }

        long percentagePeriod = Conf.DISCOVERY_TIMEOUT_MS / 100;

        mDiscoveryProgressUpdater = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ui(new Runnable() {
                    @Override
                    public void run() {
                        uiDiscoverProgress.incrementProgressBy(1);
                    }
                });
            }
        }, 0, percentagePeriod, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDiscoveryFinished(boolean success) {
        Log.i(TAG, "Discovery FINISHED (success = " + success + ")");

        ui(new Runnable() {
            @Override
            public void run() {
                uiDiscoverProgressContainer.setVisibility(View.GONE);
            }
        });

        if (mDiscoveryProgressUpdater != null) {
            mDiscoveryProgressUpdater.cancel(true);
            mDiscoveryProgressUpdater = null;
        }
    }

    private void handleAddServerButtonClick() {
        Log.v(TAG, "Clicked add server button");
        AddServerFragment addServerFragment = new AddServerFragment();
        addServerFragment.show(getActivity().getSupportFragmentManager(), AddServerFragment.FRAGMENT_TAG);
    }

    private void handleStartDiscoveryButtonClick() {
        Log.v(TAG, "Clicked discoverAndWait server button");

        // Ask confirmation

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.discover_dialog, null);
        final EditText uiDiscoverPort = dialogView.findViewById(R.id.uiDiscoverPort);
        uiDiscoverPort.setText(String.valueOf(Conf.DEFAULT_PORT));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v(TAG, "User confirmed discovery, starting it...");
                startDiscovery(IntUtils.parseString(
                        uiDiscoverPort.getText().toString(),
                        Conf.DEFAULT_PORT)
                );
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.w(TAG, "Not performing discovery, cancelled by the user");
            }
        });


        builder.setView(dialogView);

        builder.setTitle(R.string.start_discovery_dialog_title);
//        builder.setMessage(R.string.start_discovery_dialog_message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleStopDiscoveryButtonClick() {
        stopDiscovery();
    }

    private void startDiscovery(int port) {
        synchronized (mDiscovererLock) {
            Log.v(TAG, "ServersFragment.startDiscovery()");
            mDiscoverer = new MinimoteServerDiscoverer(this, port);
            mDiscoverer.startDiscovery(Conf.DISCOVERY_TIMEOUT_MS);
        }
    }

    private void stopDiscovery() {
        synchronized (mDiscovererLock) {
            Log.v(TAG, "ServersFragment.stopDiscovery()");
            if (mDiscoverer != null)
                mDiscoverer.stopDiscovery();
        }
    }

    private void showConnectionWithServerFailedAlert() {
        Context ctx = getContext();
        if (ctx == null)
            return;

        new AlertDialog.Builder(ctx)
                .setTitle(R.string.connection_with_server_failed_dialog_title)
                .setMessage(R.string.connection_with_server_failed_dialog_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
