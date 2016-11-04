package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by Angeletou on 31/10/2016.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    String supplmail;
    int price;
    int sales;
    int quant;
    Bitmap bmpProduct;
    private Uri mCurrentUri;
    private TextView nameTextView;
    private TextView priceTextView;
    private TextView quantTextView;
    private TextView salesTextView;
    private TextView supplTextView;
    private ImageView imgImageView;
    private EditText edtOrder;
    private EditText edtShipment;
    private EditText edtSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_view);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        Button btnSales = (Button) findViewById(R.id.button_sale);
        Button btnShipment = (Button) findViewById(R.id.button_shipment);
        Button btnOrder = (Button) findViewById(R.id.button_order);
        Button btnDelete = (Button) findViewById(R.id.button_delete);

        edtSales = (EditText) findViewById(R.id.sales_edt);
        edtShipment = (EditText) findViewById(R.id.shipment_edt);
        edtOrder = (EditText) findViewById(R.id.order_edt);
        // setting the listener for the sales button
        btnSales.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String salesString = edtSales.getText().toString().trim();
                if (!TextUtils.isEmpty(salesString)) {
                    int salesAdded = Integer.parseInt(salesString);
                    boolean isSaved = saveChangesOnProduct(mCurrentUri, 0, salesAdded);
                    if (isSaved) {
                        Toast.makeText(DetailActivity.this, "Changes were saved.", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        //setting the listener for the shipment button
        btnShipment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String shipmentString = edtShipment.getText().toString().trim();
                if (!TextUtils.isEmpty(shipmentString)) {
                    int itemsAdded = Integer.parseInt(shipmentString);
                    boolean isSaved = saveChangesOnProduct(mCurrentUri, 1, itemsAdded);
                    if (isSaved) {
                        Toast.makeText(DetailActivity.this, "Changes were saved.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //setting the listener for the order button leading to a mail application
        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String orderString = edtOrder.getText().toString().trim();

                if (!TextUtils.isEmpty(orderString)) {
                    int orderInt = Integer.parseInt(orderString);
                    //send intent to mail application with the mail of the supplier and the subject  xx items to order
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{supplmail});
                    i.putExtra(Intent.EXTRA_SUBJECT, orderInt + " items of " + nameTextView.getText().toString() + " to order");
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(DetailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(DetailActivity.this, "Please enter the number of items you want to order.", Toast.LENGTH_SHORT).show();

                }


            }
        });
        //setting the listener for the delete button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }

        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PIC,
                ProductEntry.COLUMN_SALES,
                ProductEntry.COLUMN_SUPPLIER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SALES);
            int supplColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER);
            int imgColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PIC);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            price = cursor.getInt(priceColumnIndex);
            quant = cursor.getInt(quantColumnIndex);
            sales = cursor.getInt(salesColumnIndex);
            supplmail = cursor.getString(supplColumnIndex);
            byte[] imgProductArray = cursor.getBlob(imgColumnIndex);
            if (imgProductArray != null) {
                bmpProduct = BitmapFactory.decodeByteArray(imgProductArray, 0, imgProductArray.length);
                imgImageView = (ImageView) findViewById(R.id.image_profile);
                imgImageView.setImageBitmap(bmpProduct);

            }

            nameTextView = (TextView) findViewById(R.id.product_name_d);
            priceTextView = (TextView) findViewById(R.id.product_price_d);
            quantTextView = (TextView) findViewById(R.id.quantity_value_d);
            salesTextView = (TextView) findViewById(R.id.sales_value_d);
            supplTextView = (TextView) findViewById(R.id.supplier_mail_d);

            // Update the views on the screen with the values from the database
            // Update the TextViews with the attributes for the current product
            nameTextView.setText(name);
            priceTextView.setText(String.valueOf(price));
            quantTextView.setText(String.valueOf(quant));
            salesTextView.setText(String.valueOf(sales));
            supplTextView.setText(supplmail);


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameTextView.setText("");
        priceTextView.setText("");
        quantTextView.setText("");
        salesTextView.setText("");
        supplTextView.setText("");

    }

    public boolean saveChangesOnProduct(Uri currentUri, int salesOrShipments, int numOfItems) {
        boolean isUpdated;
        ContentValues values = new ContentValues();
        if (salesOrShipments == 0) {
            if (quant - numOfItems >= 0) {
                values.put(ProductEntry.COLUMN_QUANTITY, quant - numOfItems);
                values.put(ProductEntry.COLUMN_SALES, numOfItems + sales);
            } else {
                Toast.makeText(this, "you cannot sell more items than what you have", Toast.LENGTH_SHORT).show();
            }
        } else if (salesOrShipments == 1) {
            values.put(ProductEntry.COLUMN_QUANTITY, quant + numOfItems);
        }
        int rowsAffected = getContentResolver().update(currentUri, values, null, null);
        if (rowsAffected > 0)
        { isUpdated = true; } else
        { isUpdated = false; }
        return isUpdated;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to delete this item?");
        builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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


    private void deleteProduct() {
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then therhe was an error with the delete.
                Toast.makeText(this, "Delete action failed", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Delete action successful", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


