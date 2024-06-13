package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.Toast;



public class SettingsActivity extends AppCompatActivity {

    ImageButton btnBack, btnGetKey;
    EditText edtKey;
    String newKey;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btnBack);
        btnGetKey = findViewById(R.id.btnGetKey);
        edtKey = findViewById(R.id.edtKey);
//        btnErrorSettings = findViewById(R.id.btnErrorSettings);


        btnGetKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent isConnectIntent = getIntent();
                Boolean isConnect = isConnectIntent.getBooleanExtra("isConnect", false);
                newKey = String.valueOf(edtKey.getEditableText());
                if(!TextUtils.isEmpty(newKey) && !isConnect) {
                    Intent newKeyIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    newKeyIntent.putExtra("newKey",newKey);
                    newKeyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(newKeyIntent);
                }
                else if(isConnect){
                    showErrorMessage("Key trước đó đã đúng ");
                }
                else if(TextUtils.isEmpty(newKey)){
                    showErrorMessage("Ô bị trống ");
                }
            }

        });

//        btnErrorSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideErrorMessage();
//            }
//        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showErrorMessage(String message) {
//        LinearLayout errorContainer = findViewById(R.id.errorContainerSettings);
//        TextView textErrorMessage = findViewById(R.id.textErrorMessageSettings);
//
//        textErrorMessage.setText(message);
//        errorContainer.setVisibility(View.VISIBLE);

        toast = Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    private void hideErrorMessage() {
//        LinearLayout errorContainer = findViewById(R.id.errorContainerSettings);
//        errorContainer.setVisibility(View.GONE);

        if (toast != null) {
            toast.cancel();
            toast = null;  // Đặt lại biến toast về null sau khi hủy
        }
    }


}

