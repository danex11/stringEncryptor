package pl.op.danex11.stringencoder;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

//TODO swipeview

/**
 *
 */
public class DecryptorAPI22 extends AppCompatActivity {

    byte[] keyBytesFromStr;
    String decodedText;

    //copy and paste -ing
    ClipboardManager myClipboard;
    EditText givenText;
    TextView resultTextView, resultlabel;
    Button copybutton;
    EditText editKey;

    ConstraintLayout layout;

    Animation animGiven, animCopyButton, animResult, animGivenback, animKeyDecr, animKeyback, animGivenfadeDecr;


    /**
     * Algorithm setting
     *
     * @param toEncrypt
     * @return
     */
    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest hashed = MessageDigest.getInstance("md5");
            // final MessageDigest digest = MessageDigest.getInstance("sha-256");
            hashed.update(toEncrypt.getBytes());
            final byte[] bytes = hashed.digest();
            final StringBuilder sb = new StringBuilder();
            //for (int i = 0; i < bytes.length; i++) {sb.append(String.format("%02X", bytes[i]));   }
            for (byte aByte : bytes) {
                sb.append(String.format("%02X", aByte));
            }
            Log.i("tag_key_Decoder", "Decoder key " + sb.toString().toLowerCase());
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
    }

    /**
     * OnCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  LAYOUT  LAYOUT  LAYOUT
        /*
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setContentView(R.layout.decryptor_layout_materials);
            //the layout on which you are working
            layout = (ConstraintLayout) findViewById(R.id.decryptorMaterialsInnerLayout);
            // >=23
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " >= 26");
        } else {
            setContentView(R.layout.decryptor_layout);
            //the layout on which you are working
            layout = (ConstraintLayout) findViewById(R.id.decryptorInnerLayout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " < 26");
        }

         */

        setContentView(R.layout.decryptor_layout);
        layout = (ConstraintLayout) findViewById(R.id.decryptorInnerLayout);

        //copy and paste -ing
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        resultTextView = findViewById(R.id.resultTextDecr);
        editKey = findViewById(R.id.keyTextDecr);
        //given text winding and keyboard behaviour
        givenText = findViewById(R.id.givenTextDecr);
        givenText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        givenText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        copybutton = findViewById(R.id.copybuttonDecr);

        //final Animation animBounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce_anim);
        animGiven = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_given);
        animCopyButton = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_copybutton);
        animResult = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_result);
        animGivenback = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_givenback);
        animKeyDecr = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_key_decrypt);
        animKeyback = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_keyback);
        animGivenfadeDecr = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_givenfade_decrypt);

        resultTextView.setVisibility(View.INVISIBLE);
        copybutton.setVisibility(View.INVISIBLE);

        resultlabel = findViewById(R.id.resultlabelDecr);
        resultlabel.setVisibility(View.INVISIBLE);


        //reaction to specific action on this view -  keyboard "Enter" reaction

        //set password hint font to default - it has mambojumboed
        editKey.setTypeface(Typeface.DEFAULT);
        //set behaviour for keyboard on keyText
        //action on keyboard confirm
        editKey
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        boolean handled = false;
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            // handle action here
                            //action
                            // Toast.makeText(getApplicationContext(), "IME action",Toast.LENGTH_SHORT).show();
                            //hide keyboard
                            try {
                                UseKey(layout);//view?????????????
                            } finally {
                                handled = true;
                            }
                        }
                        return handled;
                    }
                });


        // todo: on text change in editKey, listen for editkey.length>0, than change keyButton style

        ConstraintSet constraintSet = new ConstraintSet();
        editKey.addTextChangedListener(new TextWatcher() {
            Button button;
            int buttonid;

            @SuppressLint("ResourceType")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("tag", "Your Text onTextChanged");
                // Fires right as the text is being changed (even supplies the range of text)

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                Log.i("tag", "Your Text beforeTextChanged");
                // Fires right before text is changing


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("tag", "Your Text afterTextChanged");
                // Fires right after the text has changed


                //  Generate USE KEY button and fire its method
                if (button != null) layout.removeView(button);
                if (editKey.length() == 0 || editKey == null) {
                    //button style "faded"
                    buttonid = R.layout.buttonoff_key;
                    //Log.i("tag", "Your Text length=0");
                } else {
                    buttonid = R.layout.buttonon_key;
                    //button style "ready"
                    //Log.i("tag", "Your Text length>0");
                }
                button = (Button) getLayoutInflater().inflate(buttonid, null);
                button.setId(buttonid);
                button.setText(R.string.key_butt_lbl);
                //if (editKey.length() == 0) {
                //button.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                //btn.setVisibility(View.INVISIBLE
                //}
                constraintSet.clone(layout);
                Log.i("constraitainset", String.valueOf(constraintSet));
                constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyTextDecr, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                constraintSet.clone(layout);
                constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyTextDecr, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);

                //add button to the layout
                layout.addView(button);
                constraintSet.clone(layout);
                constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyTextDecr, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                layout.removeView(button);
                constraintSet.clone(layout);
                constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyTextDecr, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                layout.addView(button);
                constraintSet.clone(layout);
                //constraintSet.connect(button.getId(), ConstraintSet.RIGHT, R.id.keyText, ConstraintSet.LEFT, 0);
                constraintSet.connect(button.getId(), ConstraintSet.LEFT, R.id.givenTextDecr, ConstraintSet.LEFT, 0);
                constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.givenTextDecr, ConstraintSet.BOTTOM, 15);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                //button Onclick listener
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // put code on click operation
                        //action
                        // if (givenText.length() > 0) {
                        //decode_array(findViewById(R.id.keyText));
                        UseKey(layout);

                        //UseKey();
                        //  }
                    }
                });
            }
        });


    }

    /**
     * COPY
     */
    public void Copy(View view) {
        String text;
        text = resultTextView.getText().toString();
        ClipData myClip;
        myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(getApplicationContext(), "Text Copied",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * PASTE
     */
    public void Paste(View view) {
        ClipData abc = myClipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String text = item.getText().toString();
        givenText.setText(text);
        Toast.makeText(getApplicationContext(), "Text Pasted",
                Toast.LENGTH_SHORT).show();

    }

    /**
     * CLEAR
     */
    public void ClearEditText(EditText view) {
        view.setText("");
    }

    /**
     * USE KEY
     */
    public void UseKey(View view) {
        if (editKey.length() > 0) {


            resultTextView.setVisibility(View.VISIBLE);
            copybutton.setVisibility(View.VISIBLE);
            resultlabel.setVisibility(View.VISIBLE);
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editKey.getWindowToken(), 0);

            if (resultTextView.length() > 0) {
                resultTextView.setText("");
            } else {
            }
            // ClearEditText(givenText);

            String givenCipher = givenText.getText().toString();
            Log.i("givencipher", "::" + givenCipher);
            //givenTextindViewById(R.id.givenTextDecr);
            decodedText = De_text(givenCipher);


            givenText.startAnimation(animGiven);
            //Catch Animation end
            animGiven.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //resultTextView.setText("");
                    //finger.clearAnimation();
                    // findViewById(R.id.fingerview).setVisibility(View.GONE);
                    findViewById(R.id.keyTextDecr).startAnimation(animKeyDecr);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //do stuff
                    //givenText.setVisibility(View.INVISIBLE);
                    givenText.setText("");


///////////////////////////////////
                    Log.i("De_text-decodedText:", decodedText);
                    //resultTextView.setText(decodedText);

                    //set coursot to result textview
                    resultTextView.requestFocus();

                    /*
                    if (pref.getBoolean("firstrun", true)) {
                        //finger clear animation to set visibility
                        finger.clearAnimation();
                        finger.setVisibility(ImageView.INVISIBLE);
                        finger.startAnimation(animFingeratResult);
                }
                     */
                    givenText.clearAnimation();
                    givenText.startAnimation(animGivenfadeDecr);

                }


                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });

            animGivenfadeDecr.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    givenText.startAnimation(animGivenback);
                    findViewById(R.id.keyTextDecr).startAnimation(animKeyback);
                    resultTextView.setText(decodedText);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            resultTextView.startAnimation(animResult);
            animResult.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //resultTextView.setText(decodedText);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //ClearEditText(givenText);
            //resultTextView.setVisibility(View.VISIBLE);
            copybutton.setVisibility(View.VISIBLE);
            resultlabel.setVisibility(View.VISIBLE);
            editKey.startAnimation(animKeyDecr);
            ClearEditText(givenText);


        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_enc_keyempty),
                    Toast.LENGTH_SHORT).show();
        }
        //decode(findViewById(R.id.keyText));
    }


    /**
     * DECRYPTING
     *
     * @param ciphertext
     * @return
     */
    public String De_text(String ciphertext) {
        Log.i("tag_giventodecode", "given: " + ciphertext);
        //String[] decodedTexts = new String[ciphertext.length];


        //Typed key

        // TextView givenTextView = findViewById(R.id.givenTextDecr);
        //Log.i("findgivenTxtView", "givenTxtView:" + givenTextView);
        // String givenTextStrg = givenTextView.getText().toString();
        // Log.i("givenTxtStrg", "givenTxtStrg:" + givenTextStrg);

        String KeyStrg = editKey.getText().toString();
        //Hash key
        String hashedKey = md5(KeyStrg);

        //clear keyText
        ///ClearEditText(findViewById(R.id.keyText));

        Log.i("tag_hashedKey", hashedKey);
        keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);


        String algorithmo = "AES";
        SecretKeySpec secretkey = new SecretKeySpec(keyBytesFromStr, algorithmo);
        //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
        String decipheredTextStr = "¯\\_(ツ)_/¯";


        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secretkey);

            //<<@ciphertext is a value we store as a Base64 string
            //todo this is our Base64 problem
            Log.i("tag_ciphertext", "text: " + ciphertext);
            byte[] cipherText;

            //TODO crashing for some strings
            cipherText = Base64.decode(ciphertext.getBytes(), Base64.DEFAULT);
            //char encoding??

            byte[] decipheredText = cipher.doFinal(cipherText);
            if (cipherText.length > 0) {
                decipheredTextStr = decodeUTF8(decipheredText);
            }
            //decodedTexts = decipheredTextStr;
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            // decodedText = "¯\\_(ツ)_/¯";// ( ఠ ͟ʖ ఠ) ";
            // decipheredTextStr = "¯\\_(ツ)_/¯";
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return decipheredTextStr;
    }

    /**
     * * HELPING METHOD: making String from bytes
     *
     * @param bytes
     * @return
     */
    String decodeUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();

    }

}


