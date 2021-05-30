package in.aifi.mocr.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.mlkit.vision.text.Text;

import in.aifi.mocr.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SheetFragment extends Fragment {

    public SheetFragment() {
        // Required empty public constructor
    }

    public static SheetFragment newInstance() {
        SheetFragment fragment = new SheetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sheet, container, false);
    }
}