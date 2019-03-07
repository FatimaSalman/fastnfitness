package com.easyfitness.bodymeasures;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.Keyboard;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BodyPartDetailsFragment extends Fragment {
	Button addButton = null;
	EditText measureEdit = null;
	EditText dateEdit = null;
	ExpandedListView measureList = null;
	Toolbar bodyToolbar = null;
	private String name;
	private int mBodyPartID;
	private LineChart mChart = null;
	private Graph mGraph = null;
	private DAOBodyMeasure mBodyMeasureDb = null;
	private BodyPart mBodyPart = null;

	DatePickerDialogFragment mDateFrag = null;

	private void showDatePickerFragment() {
		if (mDateFrag == null) {
			mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
		}

		FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
		mDateFrag.show(ft, "dialog");
	}

	private DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
		}
	};

	private OnClickListener onClickAddMeasure = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!measureEdit.getText().toString().isEmpty()) {

				Date date = DateConverter.editToDate(dateEdit.getText().toString());

                mBodyMeasureDb.addBodyMeasure(date, mBodyPartID, Float.valueOf(measureEdit.getText().toString()), getProfile().getId());
				refreshData();
				measureEdit.setText("");

				Keyboard.hide(getContext(), v);
			} else {
				KToast.errorToast(getActivity(), "Please enter a measure", Gravity.BOTTOM, KToast.LENGTH_SHORT);

			}
		}
	};
	private OnClickListener clickDateEdit = new OnClickListener() {
		@Override
		public void onClick(View v) {
			showDatePickerFragment();
		}
	};
	private OnFocusChangeListener focusDateEdit = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				showDatePickerFragment();
			}
		}
	};
	private OnItemLongClickListener itemlongclickDeleteRecord = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> listView, View view,
									   int position, long id) {

			// Get the cursor, positioned to the corresponding row in the result set
			//Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			final long selectedID = id;

			String[] profilListArray = new String[1]; // un seul choix
			profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);

			AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getActivity());
			itemActionbuilder.setTitle("").setItems(profilListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					switch (which) {
						// Delete
						case 0:
							mBodyMeasureDb.deleteMeasure(selectedID);
							refreshData();
                            KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + selectedID, Gravity.BOTTOM, KToast.LENGTH_SHORT);
                            break;
						default:
					}
				}
			});
			itemActionbuilder.show();

			return true;
		}
	};

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static BodyPartDetailsFragment newInstance(int bodyPartID) {
		BodyPartDetailsFragment f = new BodyPartDetailsFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("bodyPartID", bodyPartID);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bodytracking_details_fragment, container, false);

		addButton = view.findViewById(R.id.buttonAddWeight);
		measureEdit = view.findViewById(R.id.editWeight);
		dateEdit= view.findViewById(R.id.profilEditDate);
		measureList = view.findViewById(R.id.listWeightProfil);
		bodyToolbar = view.findViewById(R.id.bodyTrackingDetailsToolbar);

		/* Initialisation BodyPart */
        mBodyPartID = getArguments().getInt("bodyPartID", 0);
		mBodyPart = new BodyPart(mBodyPartID);

		/* Initialisation des boutons */
		addButton.setOnClickListener(onClickAddMeasure);
		dateEdit.setOnClickListener(clickDateEdit);
		dateEdit.setOnFocusChangeListener(focusDateEdit);
		measureList.setOnItemLongClickListener(itemlongclickDeleteRecord);

		/* Initialisation des evenements */

		// Add the other graph
		mChart = view.findViewById(R.id.weightChart);
		mChart.setDescription(null);
		mGraph = new Graph(getContext(), mChart, "");
		mBodyMeasureDb = new DAOBodyMeasure(view.getContext());

		// Set Initial text
		dateEdit.setText(DateConverter.currentDate());

		((MainActivity)getActivity()).getActivityToolbar().setVisibility(View.GONE);
		bodyToolbar.setTitle(getContext().getString(mBodyPart.getResourceNameID()));
		bodyToolbar.setNavigationIcon(R.drawable.ic_back);
		bodyToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		refreshData();
	}

	private void DrawGraph(List<BodyMeasure> valueList) {

		// Recupere les enregistrements
		if (valueList.size() < 1) { mChart.clear(); return; }

		ArrayList<Entry> yVals = new ArrayList<Entry>();

        float minBodyMeasure = -1;

		for (int i = valueList.size() - 1; i >= 0; i--) {
			Entry value = new Entry((float)DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
			yVals.add(value);
            if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
				minBodyMeasure = valueList.get(i).getBodyMeasure();
        }

		mGraph.draw(yVals);
    }
	
	/*  */
	private void FillRecordTable(List<BodyMeasure> valueList) {
		Cursor oldCursor = null;

		if(valueList.isEmpty()) {
			//Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
			measureList.setAdapter(null);
		} else {
			// ...
			if ( measureList.getAdapter() == null ) {
				BodyMeasureCursorAdapter mTableAdapter = new BodyMeasureCursorAdapter (this.getView().getContext(), mBodyMeasureDb.getCursor(), 0, itemClickDeleteRecord);
				measureList.setAdapter(mTableAdapter);
			} else {
				oldCursor = ((BodyMeasureCursorAdapter) measureList.getAdapter()).swapCursor(mBodyMeasureDb.getCursor());
				if (oldCursor!=null)
					oldCursor.close();
			}
		}
	}

	public String getName() { 
		return getArguments().getString("name");
	}

	private void refreshData(){
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfile() != null) {
                List<BodyMeasure> valueList = mBodyMeasureDb.getBodyPartMeasuresList(mBodyPartID, getProfile());
				DrawGraph(valueList);
				// update table
				FillRecordTable(valueList);
			}
		}
	}

	private BtnClickListener itemClickDeleteRecord = new BtnClickListener() {
		@Override
		public void onBtnClick(long id) {
			showDeleteDialog(id);
		}
	};

	private void showDeleteDialog(final long idToDelete) {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						mBodyMeasureDb.deleteMeasure(idToDelete);
						refreshData();
						Toast.makeText(getActivity(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT)
								.show();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage(getResources().getText(R.string.DeleteRecordDialog)).setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
				.setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener).show();

	}

	private Profile getProfile()
	{
		return ((MainActivity)getActivity()).getCurrentProfil();
	}

	public Fragment getFragment() {
		return this;
	}

	/*@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) refreshData();
	}*/
}
