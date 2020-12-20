package com.dev.shreyansh.cargocontroller;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String TAG = ControlFragment.class.getSimpleName();
    private static final String ENABLED = "ENABLED";


    DatabaseReference status;
    Context context;


    private OnFragmentInteractionListener mListener;
    private TextView connectionStatus;
    private ValueEventListener listener;

    private Button up;
    private Button breakB;
    private Button left;
    private Button right;
    private Button reverse;
    private Button waitForDelivery;
    private Button reset;

    private CardView resetCard;
    private boolean showButton = false;


    public ControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        connectionStatus = view.findViewById(R.id.connection);
        up = view.findViewById(R.id.up);
        reverse = view.findViewById(R.id.reverse);
        breakB = view.findViewById(R.id.stop);
        left = view.findViewById(R.id.left);
        right = view.findViewById(R.id.right);
        reset = view.findViewById(R.id.reset);
        resetCard = view.findViewById(R.id.reset_card);

        waitForDelivery = view.findViewById(R.id.wait_for_delivery);

        status = FirebaseDatabase.getInstance().getReference().child("CONTROL");

        status.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer data = dataSnapshot.child("ENABLED").getValue(Integer.class);
                if (data == 1) {
                    status.child("GO").child("BREAK").setValue(1);
                    status.child("GO").child("UP").setValue(0);
                    status.child("GO").child("REVERSE").setValue(0);
                    status.child("GO").child("LEFT").setValue(0);
                    status.child("GO").child("RIGHT").setValue(0);
                    breakB.setBackgroundColor(Color.GREEN);
                    up.setBackgroundColor(getResources().getColor(R.color.primary));
                    reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                    left.setBackgroundColor(getResources().getColor(R.color.primary));
                    right.setBackgroundColor(getResources().getColor(R.color.primary));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer data = dataSnapshot.child("ENABLED").getValue(Integer.class);
                Log.i(TAG, String.valueOf(data));
                if(data == 1) {
                    connectionStatus.setBackgroundColor(Color.GREEN);
                    connectionStatus.setText("Connected");
                    connectionStatus.setTextColor(Color.WHITE);
                    changeControlsStatus(true);
                } else {
                    connectionStatus.setBackgroundColor(Color.RED);
                    connectionStatus.setText("Not Connected!");
                    connectionStatus.setTextColor(Color.WHITE);
                    changeControlsStatus(false);
                }

                data = dataSnapshot.child("WFD").getValue(Integer.class);
                if(data == 1) {
                    waitForDelivery.setBackgroundColor(Color.RED);
                    waitForDelivery.setText("Waiting for delivery to complete.");
                    waitForDelivery.setTextColor(Color.WHITE);
                    status.child("GO").child("BREAK").setValue(1);
                } else {
                    waitForDelivery.setBackgroundColor(Color.GREEN);
                    waitForDelivery.setText("Reach Client");
                    waitForDelivery.setTextColor(Color.WHITE);
                }

                Integer upData = dataSnapshot.child("GO").child("UP").getValue(Integer.class);
                Integer reverseData = dataSnapshot.child("GO").child("REVERSE").getValue(Integer.class);
                Integer leftData = dataSnapshot.child("GO").child("LEFT").getValue(Integer.class);
                Integer rightData = dataSnapshot.child("GO").child("RIGHT").getValue(Integer.class);
                Integer on = 1;
                Log.i(TAG, String.valueOf(up) + reverseData  + leftData + rightData);
                if(upData == on || reverseData == on  || leftData == on || rightData == on) {
                    status.child("WFD").setValue(0);
                }

                if (upData != on && reverseData != on  && leftData != on && rightData != on){
                    status.child("GO").child("BREAK").setValue(1);
                    breakB.setBackgroundColor(Color.GREEN);
                } else {
                    breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                    status.child("GO").child("BREAK").setValue(0);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getDetails());
            }
        };
        status.addValueEventListener(listener);

        setMovementListener();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            this.context = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        status.removeEventListener(listener);
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

    private void setMovementListener() {
        final DatabaseReference reference = status.child("GO");
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("UP").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(0);
                            reference.child("UP").setValue(1);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(0);
                            up.setBackgroundColor(Color.GREEN);
                            breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                            Log.i("--MOVEMENT--", "UP sent to Firebase.");
                        }
                        else {
                            reference.child("UP").setValue(0);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        breakB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("BREAK").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(1);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(0);
                            breakB.setBackgroundColor(Color.GREEN);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                            Log.i("--MOVEMENT--", "BREAK sent to Firebase.");
                        }
                        else {
                            reference.child("BREAK").setValue(0);
                            breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("REVERSE").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(0);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(1);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(0);
                            reverse.setBackgroundColor(Color.GREEN);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                            breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                            Log.i("--MOVEMENT--", "REVERSE sent to Firebase.");
                        }
                        else {
                            reference.child("REVERSE").setValue(0);
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("LEFT").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(0);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(1);
                            reference.child("RIGHT").setValue(0);
                            left.setBackgroundColor(Color.GREEN);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                            breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                            Log.i("--MOVEMENT--", "LEFT sent to Firebase.");
                        }
                        else {
                            reference.child("LEFT").setValue(0);
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child("RIGHT").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(0);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(1);
                            right.setBackgroundColor(Color.GREEN);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                            breakB.setBackgroundColor(getResources().getColor(R.color.primary));
                            Log.i("--MOVEMENT--", "RIGHT sent to Firebase.");
                        }
                        else {
                            reference.child("RIGHT").setValue(0);
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        waitForDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.child("WFD").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(Integer.class)==0) {
                            reference.child("BREAK").setValue(1);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(0);
                            status.child("WFD").setValue(1);
                            waitForDelivery.setBackgroundColor(Color.RED);
                            waitForDelivery.setText("Waiting for delivery to complete.");
                            waitForDelivery.setTextColor(Color.WHITE);
                            breakB.setBackgroundColor(Color.GREEN);
                            up.setBackgroundColor(getResources().getColor(R.color.primary));
                            reverse.setBackgroundColor(getResources().getColor(R.color.primary));
                            left.setBackgroundColor(getResources().getColor(R.color.primary));
                            right.setBackgroundColor(getResources().getColor(R.color.primary));
                            showButton = true;

                        } else {
                            status.child("WFD").setValue(0);
                            reference.child("BREAK").setValue(1);
                            reference.child("UP").setValue(0);
                            reference.child("REVERSE").setValue(0);
                            reference.child("LEFT").setValue(0);
                            reference.child("RIGHT").setValue(0);
                            waitForDelivery.setBackgroundColor(Color.GREEN);
                            waitForDelivery.setText("Reach Client.");
                            waitForDelivery.setTextColor(Color.WHITE);
                            if(showButton) {
                                resetCard.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_placeholder, new StatusFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    public void changeControlsStatus(boolean status) {
        up.setEnabled(status);
        left.setEnabled(status);
        right.setEnabled(status);
        breakB.setEnabled(status);
        reverse.setEnabled(status);
        waitForDelivery.setEnabled(status);

    }
}
