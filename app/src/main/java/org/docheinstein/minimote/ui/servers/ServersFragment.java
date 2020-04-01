package org.docheinstein.minimote.ui.servers;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.docheinstein.minimote.R;
import org.docheinstein.minimote.lifecycle.AppLifecycleManager;
import org.docheinstein.minimote.ui.base.MinimoteFragment;
import org.docheinstein.minimote.commons.Conf;
import org.docheinstein.minimote.database.DB;
import org.docheinstein.minimote.database.server.MinimoteServerEntity;
import org.docheinstein.minimote.discovery.MinimoteDiscoveredServer;
import org.docheinstein.minimote.discovery.MinimoteServerDiscoverer;
import org.docheinstein.minimote.utils.IntUtils;
import org.docheinstein.minimote.utils.NetUtils;
import org.docheinstein.minimote.utils.StringUtils;
import org.docheinstein.minimote.utils.ViewUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServersFragment
        extends MinimoteFragment
        implements MinimoteServerDiscoverer.MinimoteServerDiscovererListener {

    private static final String TAG = "ServersFragment";

    private ServerListAdapter uiServerListAdapter;
    private RecyclerView.LayoutManager uiServerListLayoutManager;
    private RecyclerView uiServerList;

    private View uiDiscoveryContainer;
    private ProgressBar uiDiscoveryProgress;
    private ScheduledFuture mDiscoveryProgressUpdater;

    private final Object mDiscovererLock = new Object();
    private MinimoteServerDiscoverer mDiscoverer;

    private static AutoConnectTracker sAutoConnectTracker = new AutoConnectTracker();

    private static class AutoConnectTracker implements AppLifecycleManager.AppLifecycleListener {
        private static final String TAG = "AutoConnectTracker";

        private final AtomicBoolean mAutoConnectTried = new AtomicBoolean(false);

        @Override
        public void onAppStart() {
        }

        @Override
        public void onAppStop() {
            // Reset auto connect flag so that the next time
            // the auto connection will be tried again
            Log.v(TAG, "Resetting auto connect flag since app has been destroyed");
            mAutoConnectTried.set(false);
        }

        @Override
        public void onAppDestroy() {
            Log.v(TAG, "onAppDestroy: doing nothing");
        }

        boolean useAutoConnect() {
            return mAutoConnectTried.compareAndSet(false, true);
        }
    }

    public static class AddServerFragment extends DialogFragment {
        static final String FRAGMENT_TAG = "add_server_fragment";

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Activity activity = getActivity();

            if (activity == null) {
                return null;
            }

            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_server_dialog, null);

            final EditText uiServerAddress = dialogView.findViewById(R.id.uiAddress);
            final EditText uiServerPort = dialogView.findViewById(R.id.uiPort);

            uiServerPort.setText(String.valueOf(Conf.Connection.DEFAULT_DISCOVER_PORT));

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            return builder
                    .setTitle(R.string.add_server_dialog_title)
                    .setView(dialogView)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String serverAddress =
                                    uiServerAddress.getText().toString();
                            final Integer serverPort =
                                    IntUtils.parseString(
                                            uiServerPort.getText().toString(),
                                            Conf.Connection.DEFAULT_DISCOVER_PORT
                                    );

                            if (!(new NetUtils.AddressPort(serverAddress, serverPort).isValid())) {
                                showInvalidAdditionAlert();
                                return;
                            }

                            Log.d(TAG, "Trying to add server with address: " + serverAddress + ":" + serverPort);

                            // Add server
                            DB.getInstance().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Valid IPv4, adding minimote server to DB");
                                    MinimoteServerEntity serverEntity = new MinimoteServerEntity(
                                            serverAddress, serverPort, null, null, false);
                                    DB.getInstance().servers().addOrReplace(serverEntity);
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
            holder.uiServerEditButton.setOnClickListener(new View.OnClickListener() {
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
            ImageView uiServerEditButton;
            TextView uiServerDisplayName;
            TextView uiServerAddress;
            View.OnClickListener mRowClickListener;

            ServerViewHolder(@NonNull View itemView) {
                super(itemView);
                uiServerEditButton = itemView.findViewById(R.id.uiEditButton);
                uiServerDisplayName = itemView.findViewById(R.id.uiDisplayName);
                uiServerAddress = itemView.findViewById(R.id.uiAddress);

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.servers, container, false);

        Log.v(TAG, "ServersFragment.onCreateView() [" + hashCode() + "]");


        // Add server
        view.findViewById(R.id.uiAddServerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddServerButtonClick();
            }
        });

        // Start discovery
        view.findViewById(R.id.uiDiscoverServersButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartDiscoveryButtonClick();
            }
        });

        // Stop discovery
        view.findViewById(R.id.uiDiscoveryStopButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Stop discovery required by the user");
                handleStopDiscoveryButtonClick();
            }
        });

        uiServerList = view.findViewById(R.id.uiServerList);
        uiServerList.setHasFixedSize(true);

        uiServerListLayoutManager = new LinearLayoutManager(getContext());
        uiServerList.setLayoutManager(uiServerListLayoutManager);

        uiServerListAdapter = new ServerListAdapter();
        uiServerList.setAdapter(uiServerListAdapter);

        uiDiscoveryContainer = view.findViewById(R.id.uiDiscoveryContainer);
        uiDiscoveryProgress = view.findViewById(R.id.uiDiscoveryProgress);

        DB.getInstance().servers().getAllObservable().observe(
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

        autoConnectIfNeeded();
//        Bundle extras = getArguments();
//        if (extras != null) {
//            Log.v(TAG, "There are arguments");
//            boolean autoConnect = extras.getBoolean(AUTO_CONNECT_EXTRA, AUTO_CONNECT_EXTRA_DEFAULT_VALUE);
//            Log.v(TAG, "Auto connect in args: " + autoConnect);
//            if (autoConnect) {
//                autoConnectIfNeeded();
//            }
//        }

        AppLifecycleManager.getInstance().addListener(sAutoConnectTracker);
        return view;
    }

    @Override
    public void onServerDiscovered(final MinimoteDiscoveredServer server) {
        Log.i(TAG, "Discovered server: " + server);

        // Add server (if necessary)
        DB.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                MinimoteServerEntity serverEntity =
                        DB.getInstance().servers().get(server.getAddress(), server.getPort());

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

                DB.getInstance().servers().addOrReplace(serverEntity);
            }
        });
    }

    @Override
    public void onDiscoveryStarted() {
        Log.i(TAG, "Discovery STARTED");

        ui(new Runnable() {
            @Override
            public void run() {
                uiDiscoveryProgress.setProgress(0);
                ViewUtils.show(uiDiscoveryContainer);
            }
        });

        if (mDiscoveryProgressUpdater != null) {
            Log.v(TAG, "Interrupting progress bar updater");
            mDiscoveryProgressUpdater.cancel(true);
        }

        long percentagePeriod = Conf.Discovery.TIMEOUT / 100;

        mDiscoveryProgressUpdater = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ui(new Runnable() {
                    @Override
                    public void run() {
                        uiDiscoveryProgress.incrementProgressBy(1);
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
                ViewUtils.hide(uiDiscoveryContainer);
            }
        });

        if (mDiscoveryProgressUpdater != null) {
            mDiscoveryProgressUpdater.cancel(true);
            mDiscoveryProgressUpdater = null;
        }
    }

    private void autoConnectIfNeeded() {
        if (sAutoConnectTracker.useAutoConnect()) {
            Log.d(TAG, "Auto connect required, " +
                    "checking if there is a server with the auto-connect flag... [" + hashCode() + "]");
            DB.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    final MinimoteServerEntity autoConnectServer =
                            DB.getInstance().servers().getAutoConnectionRequired();

                    if (autoConnectServer == null)  {
                        Log.d(TAG, "No servers with the auto connection flag, doing nothing");
                        return;
                    }

                    // Attempt connection
                    Log.d(TAG, "Auto connection required for server " + autoConnectServer + ", attempting so");

                    ui(new Runnable() {
                        @Override
                        public void run() {
                            ServersFragmentDirections.ActionController action =
                                    ServersFragmentDirections.actionController(
                                            autoConnectServer.address,
                                            autoConnectServer.port);
                            NavHostFragment.findNavController(ServersFragment.this).navigate(action);
                        }
                    });

                }
            });
        } else {
            Log.v(TAG, "Auto connect already tried");
        }
    }

    private void handleAddServerButtonClick() {
        FragmentActivity activity = getActivity();

        if (activity == null) {
            Log.w(TAG, "Null activity!?");
            return;
        }

        Log.v(TAG, "Clicked add server button");
        AddServerFragment addServerFragment = new AddServerFragment();
        addServerFragment.show(activity.getSupportFragmentManager(), AddServerFragment.FRAGMENT_TAG);
    }

    private void handleStartDiscoveryButtonClick() {
        FragmentActivity activity = getActivity();

        if (activity == null) {
            Log.w(TAG, "Null activity!?");
            return;
        }

        Log.v(TAG, "Clicked discoverAndWait server button");

        // Ask confirmation

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.discover_dialog, null);
        final EditText uiDiscoverPort = dialogView.findViewById(R.id.uiDiscoverPort);
        uiDiscoverPort.setText(String.valueOf(Conf.Connection.DEFAULT_DISCOVER_PORT));

        new AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle(R.string.start_discovery_dialog_title)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.v(TAG, "User confirmed discovery, starting it...");
                    startDiscovery(IntUtils.parseString(
                            uiDiscoverPort.getText().toString(),
                            Conf.Connection.DEFAULT_DISCOVER_PORT)
                    );
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.w(TAG, "Not performing discovery, cancelled by the user");
                }
            })
            .create()
            .show();
    }

    private void handleStopDiscoveryButtonClick() {
        stopDiscovery();
    }

    private void startDiscovery(int port) {
        synchronized (mDiscovererLock) {
            Log.v(TAG, "ServersFragment.startDiscovery()");
            mDiscoverer = new MinimoteServerDiscoverer(this, port);
            mDiscoverer.startDiscovery(Conf.Discovery.TIMEOUT);
        }
    }

    private void stopDiscovery() {
        synchronized (mDiscovererLock) {
            Log.v(TAG, "ServersFragment.stopDiscovery()");
            if (mDiscoverer != null)
                mDiscoverer.stopDiscovery();
        }
    }
}
