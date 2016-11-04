package com.example.android.inventoryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Angeletou on 31/10/2016.
 */

public class AddActivity extends AppCompatActivity implements Parcelable {

    private static final String STATE_URI = "STATE_URI";
    private static final int PICK_IMAGE_REQUEST = 0;
    EditText nameEditText;
    EditText priceEditText;
    EditText quantEditText;
    EditText supplEditText;
    ImageView imgAddView;
    String nameString;
    String priceString;
    String quantString;
    String supplMail;
    int priceInt;
    int quantInt;
    Uri newProductUri;
    Bitmap productImg;
    boolean mProductChanged;
    private Uri mUri;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductChanged = true;
            return false;
        }
    };

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        Intent intent = getIntent();
        // Find all relevant views that we will need to read user input from
        nameEditText = (EditText) findViewById(R.id.edit_name);
        priceEditText = (EditText) findViewById(R.id.edit_price);
        quantEditText = (EditText) findViewById(R.id.edit_quantity);
        supplEditText = (EditText) findViewById(R.id.edit_supplier);
        imgAddView = (ImageView) findViewById(R.id.image_add);

        ViewTreeObserver viewTreeObserver = imgAddView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgAddView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                productImg = getBitmapFromUri(mUri);
                imgAddView.setImageBitmap(productImg);
            }
        });

        imgAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });


        // Read from input fields

        Button saveBtn = (Button) findViewById(R.id.button_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                // Use trim to eliminate leading or trailing white space
                nameString = nameEditText.getText().toString().trim();
                priceString = priceEditText.getText().toString().trim();
                quantString = quantEditText.getText().toString().trim();
                supplMail = supplEditText.getText().toString().trim();

                if ((!TextUtils.isEmpty(nameString)) && (!TextUtils.isEmpty(priceString)) &&
                        (!TextUtils.isEmpty(quantString)) && (!TextUtils.isEmpty(supplMail)) && (productImg != null)) {

                        priceInt = Integer.parseInt(priceString);
                        quantInt = Integer.parseInt(quantString);

                    if ((priceInt < 0) || (quantInt < 0)) {
                        Toast.makeText(AddActivity.this, "please fill in positive numbers for quantity and price", Toast.LENGTH_SHORT).show();
                    } else if (!isEmailValid(supplMail)) {
                        Toast.makeText(AddActivity.this, "please fill in a valid email address", Toast.LENGTH_SHORT).show();
                    } else {
                        newProductUri = saveProduct();
                        finish();
                    }
                } else {
                    Toast.makeText(AddActivity.this, "please fill in all the gaps", Toast.LENGTH_SHORT).show();
                }


            }
        });

        //if the user attempts to exit, display a dialog "save or discard?"

    }

    private Uri saveProduct() {


        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRICE, priceInt);
        values.put(ProductEntry.COLUMN_QUANTITY, quantInt);
        values.put(ProductEntry.COLUMN_SUPPLIER, supplMail);
        values.put(ProductEntry.COLUMN_SALES, 0);


        if (productImg != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean a = productImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            values.put(ProductEntry.COLUMN_PIC, byteArray);
        }


        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(AddActivity.this, " The product could not be added.", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            return newUri;
        }

    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = imgAddView.getWidth();
        int targetH = imgAddView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;


            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e("AddActivity", "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e("AddActivity", "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i("AddActivity", "Uri: " + mUri.toString());
                productImg = getBitmapFromUri(mUri);
                imgAddView.setImageBitmap(productImg);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUri != null)
            outState.putString(STATE_URI, mUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mUri = Uri.parse(savedInstanceState.getString(STATE_URI));
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { }
}
