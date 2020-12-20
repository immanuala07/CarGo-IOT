package com.dev.shreyansh.cargocontroller;

import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatusFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = StatusFragment.class.getSimpleName();
    private static final String TARGET_DEVICE = "00:21:13:03:B9:3B HC-05";


    private TextView connectionStatus;
    private Switch bluetoothToggle;
    private Spinner userList;
    private Button connectToDevice;
    private Button go;
    private List<String> userNames;
    private String[] userRFID;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ArrayAdapter<String> adapter;
    private Context context;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket bluetoothSocket;

    private BluetoothAdapter bluetoothAdapter;

    private OnFragmentInteractionListener mListener;

    public StatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment StatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        userNames = new ArrayList<String>();
        userNames.add("Immanual");
        userNames.add("Nandita");
        userNames.add("Shreyansh");
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, userNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        userRFID = new String[]{"93 CD B1 02", "73 A7 B4 02", "60 C2 D5 B1"};

        bluetoothToggle = view.findViewById(R.id.bluetooth_toggle);
        connectToDevice = view.findViewById(R.id.connect_button);
        userList = view.findViewById(R.id.user_name);
        userList.setAdapter(adapter);
        connectionStatus = view.findViewById(R.id.connected_text);
        go = view.findViewById(R.id.confirm_user);

        userList.setVisibility(View.GONE);
        go.setVisibility(View.GONE);

        PackageManager pm = context.getPackageManager();
        boolean hasBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        Log.i(TAG, String.valueOf(hasBluetooth));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter.isEnabled())
            bluetoothToggle.setChecked(true);

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothToggle.isChecked()) {
                    bluetoothAdapter.enable();
                    bluetoothAdapter.startDiscovery();
                }
                if(pairedDeviceList()) {
                    userList.setVisibility(View.VISIBLE);
                    go.setVisibility(View.VISIBLE);
                }

            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });
        bluetoothToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    bluetoothAdapter.enable();
                    bluetoothAdapter.startDiscovery();

                }
                else {
                    bluetoothAdapter.disable();
                }
            }
        });

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("BT", device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private boolean pairedDeviceList() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if ( pairedDevices.size() > 0 ) {
            for ( BluetoothDevice bt : pairedDevices ) {
                Log.i("-->>>", bt.getName()+ bt.getAddress());
                if(bt.getAddress().equals("00:21:13:03:B9:3B")){
                    return connectToCarGO("00:21:13:03:B9:3B");
                }
            }
        } else {
            Toast.makeText(context, "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private synchronized boolean connectToCarGO(String address) {
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            bluetoothAdapter.cancelDiscovery();
            bluetoothSocket.connect();
            connectionStatus.setText("Connected to Smart Shelf.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException : ", e);
        } catch (Exception e) {
            Log.e(TAG, "Error : ", e);
        }
        return false;
    }

    public void sendData() {
        try {
            if(bluetoothSocket.isConnected()) {
                bluetoothSocket.getOutputStream().write(userRFID[(int) userList.getSelectedItemId()].getBytes());
                Log.i(TAG, userRFID[(int) userList.getSelectedItemId()]);
                bluetoothSocket.close();
                bluetoothAdapter.disable();
                bluetoothToggle.setChecked(false);
                Toast.makeText(context, "RFID of receiver shared to CarGO", Toast.LENGTH_LONG).show();
                android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, new ControlFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
            else {
//                if (pairedDeviceList())
//                    sendData();
                bluetoothSocket.connect();
            }

        } catch (IOException e) {
            Log.e(TAG, "IOException : ", e);
        } catch (Exception e) {
            Log.e(TAG, "Error : ", e);
        }
    }
}
