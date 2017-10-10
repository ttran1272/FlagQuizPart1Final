package edu.orangecoastcollege.cs273.flagquiz;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 10;

    private Button[] mButtons = new Button[4];
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // TODO: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        // TODO: Load all the countries from the JSON file using the JSONLoader
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON file", e);
        }

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
        int size = mAllCountriesList.size();
        int randomPosition;
        Country randomCountry;
        while (mQuizCountriesList.size() < FLAGS_IN_QUIZ)
        {
            // Generate random position:
            randomPosition = rng.nextInt(size);
            randomCountry = mAllCountriesList.get(randomPosition);
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
            Collections.shuffle(mAllCountriesList);
        }
        while (mAllCountriesList.subList(0, mButtons.length).contains(mCorrectCountry));

        // TODO: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // TODO: in the all countries list
        for (int i = 0; i < mButtons.length; i++)
        {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mAllCountriesList.get(i).getName());
        }

        // TODO: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mButtons.length)].setText(mCorrectCountry.getName());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
