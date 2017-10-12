package edu.orangecoastcollege.cs273.flagquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 10;

    private Button[] mButtons = new Button[8];
    private LinearLayout[] mLayouts = new LinearLayout[4];
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private List<Country> mFilteredCountriesList; // countries filtered by selected region
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    private int mChoices; // stores how many choices (buttons) selected
    private String mRegion; // stores what region is selected

    // Keys used in preferences.xml
    private static final String CHOICES = "pref_numberOfChoices";
    private static final String REGIONS =" pref_regions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Let's register the OnSharedPreferencesChangeListener
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);


        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // TODO: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);
        mFlagImageView = (ImageView) findViewById(R.id.flagImageView);

        // TODO: Put all 4 buttons in the array (mButtons)
        mButtons[0] = (Button) findViewById(R.id.button);
        mButtons[1] = (Button) findViewById(R.id.button2);
        mButtons[2] = (Button) findViewById(R.id.button3);
        mButtons[3] = (Button) findViewById(R.id.button4);
        mButtons[4] = (Button) findViewById(R.id.button5);
        mButtons[5] = (Button) findViewById(R.id.button6);
        mButtons[6] = (Button) findViewById(R.id.button7);
        mButtons[7] = (Button) findViewById(R.id.button8);

        mLayouts[0] = (LinearLayout) findViewById(R.id.row1LinearLayout);
        mLayouts[1] = (LinearLayout) findViewById(R.id.row2LinearLayout);
        mLayouts[2] = (LinearLayout) findViewById(R.id.row3LinearLayout);
        mLayouts[3] = (LinearLayout) findViewById(R.id.row4LinearLayout);




        // TODO: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        // TODO: Load all the countries from the JSON file using the JSONLoader
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON file", e);
        }

        mRegion = preferences.getString(REGIONS, "All");

        mChoices = Integer.parseInt(preferences.getString(CHOICES, "4"));
        updateChoices();
        updateRegion();

        // TODO: Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }

    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {

        // Reset the number of correct guesses made
        mCorrectGuesses = 0;
        // TODO: Reset the total number of guesses the user made
        mTotalGuesses = 0;
        // TODO: Clear list of quiz countries (for prior games played)
        mQuizCountriesList.clear();
        // TODO: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        // TODO: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
        int size = mFilteredCountriesList.size();
        int randomPosition;
        Country randomCountry;
        while (mQuizCountriesList.size() < FLAGS_IN_QUIZ)
        {
            // Generate random position:
            randomPosition = rng.nextInt(size);
            randomCountry = mFilteredCountriesList.get(randomPosition);
            // Determine whether to add the random country
            if (!mQuizCountriesList.contains(randomCountry))
                mQuizCountriesList.add(randomCountry);
        }
        // TODO: Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    private void loadNextFlag() {
        // TODO: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectCountry = mQuizCountriesList.remove(0);
        // TODO: Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");
        // TODO: Display current question number in the mQuestionNumberTextView
        int questionNumber = FLAGS_IN_QUIZ - mQuizCountriesList.size();
        mQuestionNumberTextView.setText(getString(R.string.question, questionNumber, FLAGS_IN_QUIZ));


        // TODO: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();

        try {
            InputStream stream = am.open(mCorrectCountry.getFileName());
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName());
            mFlagImageView.setImageDrawable(image);

        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + mCorrectCountry.getFileName(), e);
        }

        // TODO: Get an InputStream to the asset representing the next flag
        // TODO: and try to use the InputStream to create a Drawable
        // TODO: The file name can be retrieved from the correct country's file name.
        // TODO: Set the image drawable to the correct flag.

        // TODO: Shuffle the order of all the countries (use Collections.shuffle)
        do {
            Collections.shuffle(mFilteredCountriesList);
        }
        while (mFilteredCountriesList.subList(0, mChoices).contains(mCorrectCountry));

        // TODO: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // TODO: in the all countries list
        for (int i = 0; i < mChoices; i++)
        {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mFilteredCountriesList.get(i).getName());
        }

        // TODO: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mChoices)].setText(mCorrectCountry.getName());
    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    public void makeGuess(View v) {
        // TODO: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;
        // TODO: Get the country's name from the text of the button
        String guess = clickedButton.getText().toString();

        mTotalGuesses++;
        // TODO: If the guess matches the correct country's name, increment the number of correct guesses,
        if (guess.equals(mCorrectCountry.getName()))
        {
            // Disable all buttons (don't let user guess again)
            for (Button b : mButtons)
                b.setEnabled(false);

            mCorrectGuesses++;
            mAnswerTextView.setText(mCorrectCountry.getName());
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.correct_answer));

            if (mCorrectGuesses < FLAGS_IN_QUIZ)
            {
                // Wait two seconds, then load next flag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000);
            }
            else {
                // Show an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.results, mTotalGuesses, (double) mCorrectGuesses / mTotalGuesses));
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetQuiz();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();

            }

        }
        else
        {
            clickedButton.setEnabled(false);
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.incorrect_answer));
        }
        // TODO: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
        // TODO: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
        // TODO: with the statistics and an option to Reset Quiz

        // TODO: Else, the answer is incorrect, so display "Incorrect Guess!" in red
        // TODO: and disable just the incorrect button.



    }

    // Overwrite onCreateOptionsMenu to inflate the settings menu


    @Override
    // Inflates the Settings menu within MainActivity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // Responds to the user clicking the Settings menu icon
    public boolean onOptionsItemSelected(MenuItem item) {
        // Make a new Intent going to SettingsActivity
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);

        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Let's figure out what key changed
            switch (key)
            {
                case CHOICES:
                    // Read the number of choices from shared preferences
                    //String numberString = sharedPreferences.getString(CHOICES, "4");
                    //mChoices = Integer.parseInt(numberString);
                    mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES, "4"));

                    // Call method to update choices (visually)
                    updateChoices();
                    resetQuiz();
                    break;

                case REGIONS:
                    mRegion = sharedPreferences.getString(REGIONS, "All");
                    updateRegion();
                    resetQuiz();
                    break;
            }

            // Notify the user that the quiz will restart
            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();

        }
    };

    private void updateChoices() {
        // Enable/Show all the linear layouts < mChoices / 2
        // Disable/Hide all the others
        // Let's loop through all linear layouts
        for (int i = 0; i < mLayouts.length; i++) {
            if (i < mChoices / 2) {
                mLayouts[i].setEnabled(true);
                mLayouts[i].setVisibility(View.VISIBLE);
            }
            else {
                mLayouts[i].setEnabled(true);
                mLayouts[i].setVisibility(View.GONE);
            }
        }
    }

    private void updateRegion() {
        // Make a decision:
        // If the region is "All", filtered list is the same as all
        if (mRegion.equals("All"))
            mFilteredCountriesList = new ArrayList<>(mAllCountriesList);
        else
        {
            mFilteredCountriesList = new ArrayList<>();
            // Loop through all countries
            for (Country c: mAllCountriesList)
            {
                if (c.getRegion().equals(mRegion))
                    mFilteredCountriesList.add(c);
            }
        }

    }
}
