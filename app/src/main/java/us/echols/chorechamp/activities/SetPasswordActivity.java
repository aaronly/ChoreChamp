package us.echols.chorechamp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

import us.echols.chorechamp.PasswordUtility;
import us.echols.chorechamp.R;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText editTextPassword;
    private EditText editTextConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        editTextPassword = (EditText)findViewById(R.id.editText_password);
        editTextConfirm = (EditText)findViewById(R.id.editText_confirm);

        Button buttonOK = (Button)findViewById(R.id.button_save);
        Button buttonCancel = (Button)findViewById(R.id.button_cancel);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePassword();
            }
        });
    }

    @SuppressWarnings("UnusedAssignment")
    private boolean isValidPassword() {

        boolean isMatch = editTextPassword.getText().toString().equals(editTextConfirm.getText().toString());
        if(!isMatch) {
            Toast.makeText(this, getString(R.string.error_password_match), Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isLongEnough = editTextPassword.getText().toString().length() >= 8;
        if(!isLongEnough) {
            Toast.makeText(this, getString(R.string.error_password_too_short), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void savePassword() {
        if(isValidPassword()) {
            PasswordUtility passwordUtility = PasswordUtility.getInstance(this);
            passwordUtility.setPassword(editTextPassword.getText().toString().getBytes(StandardCharsets.UTF_8));
            finish();
        }
    }

    private void onCancel() {
        PasswordUtility passwordUtility = PasswordUtility.getInstance(this);
        boolean passwordExists = passwordUtility.checkForPasswordFile();
        if(!passwordExists) {
            Toast.makeText(this, getString(R.string.error_password_not_set), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
