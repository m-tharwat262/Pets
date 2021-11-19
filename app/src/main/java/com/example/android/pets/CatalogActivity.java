package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    private CursorAdapter mCursorAdapter;

    private PetDbHelper mDbHelper;
    private ListView mListView;
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        mDbHelper = new PetDbHelper(this);
        mListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(emptyView);

        mCursorAdapter = new PetCursorAdapter(this, null);
        mListView.setAdapter(mCursorAdapter);

        LoaderManager.getInstance(this).initLoader(PET_LOADER, null, this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentPetUri = new ContentUris().withAppendedId(PetEntry.CONTENT_URI, id);

                intent.setData(currentPetUri);

                startActivity(intent);

            }
        });

    }



    private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_insert_dummy_data:

                insertPet();
                return true;

            case R.id.action_delete_all_entries:

                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pet_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.editor_delete_all_pet_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.editor_delete_all_pet_successful, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        String [] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
        };

        switch (loaderID) {

            case PET_LOADER:
                return new CursorLoader(this,
                        PetEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);

            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);

    }
}