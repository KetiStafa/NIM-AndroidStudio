package com.example.nim_androidstudio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private int[] rows = {1, 3, 5, 7};
    private ImageView[][] matchstickImages;
    private RadioGroup rowRadioGroup;
    private EditText matchsticksEditText;
    private TextView statusTextView;
    private Button makeMoveButton, newGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize matchstick image views
        matchstickImages = new ImageView[][]{
                {findViewById(R.id.imageView1)},
                {findViewById(R.id.imageView4), findViewById(R.id.imageView3), findViewById(R.id.imageView2)},
                {findViewById(R.id.imageView9), findViewById(R.id.imageView5), findViewById(R.id.imageView8), findViewById(R.id.imageView6), findViewById(R.id.imageView7)},
                {findViewById(R.id.imageView16), findViewById(R.id.imageView13), findViewById(R.id.imageView10), findViewById(R.id.imageView15), findViewById(R.id.imageView14), findViewById(R.id.imageView11), findViewById(R.id.imageView12)}
        };

        // Initialize buttons
        rowRadioGroup = findViewById(R.id.rowRadioGroup);
        matchsticksEditText = findViewById(R.id.matchsticksEditText);
        statusTextView = findViewById(R.id.statusTextView);
        makeMoveButton = findViewById(R.id.makeMoveButton);
        newGameButton = findViewById(R.id.newGameButton);

        makeMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMove();
            }
        });
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
        updateUI();
    }

    private void makeMove() {
        int selectedRowId = rowRadioGroup.getCheckedRadioButtonId();
        if (selectedRowId == -1) {
            Toast.makeText(this, "Please select a row", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map the selected RadioButton ID to the corresponding row index
        int[] radioButtonIds = {
                R.id.row1RadioButton,
                R.id.row2RadioButton,
                R.id.row3RadioButton,
                R.id.row4RadioButton
        };
        int row = -1;
        for (int i = 0; i < radioButtonIds.length; i++) {
            if (radioButtonIds[i] == selectedRowId) {
                row = i;
                break;
            }
        }

        String numSticksStr = matchsticksEditText.getText().toString();
        if (numSticksStr.isEmpty()) {
            Toast.makeText(this, "Please enter the number of matchsticks to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        int numSticks = Integer.parseInt(numSticksStr);
        if (numSticks <= 0 || numSticks > rows[row]) {
            Toast.makeText(this, "Invalid number of matchsticks", Toast.LENGTH_SHORT).show();
            return;
        }

        rows[row] -= numSticks;
        updateUI();
        checkGameOver();

        if (!isGameOver()) {
            aiMove();
            updateUI();
            checkGameOver();
        }
    }

    private boolean isGameOver() {
        for (int row : rows) {
            if (row > 0) {
                return false;
            }
        }
        return true;
    }

    private void aiMove() {
        int xorSum = 0;
        for (int row : rows) {
            xorSum ^= row;
        }

        if (xorSum == 0) {
            // AI is in a losing position, make a random move
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] > 0) {
                    rows[i]--;
                    Toast.makeText(this, "AI removed 1 matchstick from row " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            // AI is in a winning position, make the optimal move
            for (int i = 0; i < rows.length; i++) {
                int target = rows[i] ^ xorSum;
                if (target < rows[i]) {
                    int remove = rows[i] - target;
                    if (remove == rows[i] && countRowsWithSameMatchsticks(rows[i]) == 2) {
                        // Check if removing all matchsticks and two rows have the same amount
                        int otherRow = findOtherRowWithSameMatchsticks(rows[i]);
                        if (rows[otherRow] > 0) {
                            rows[otherRow]--;
                            Toast.makeText(this, "AI removed 1 matchstick from row " + (otherRow + 1), Toast.LENGTH_SHORT).show();
                        } else {
                            // If the other row is already empty, remove from the current row
                            rows[i]--;
                            Toast.makeText(this, "AI removed 1 matchstick from row " + (i + 1), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rows[i] = target;
                        Toast.makeText(this, "AI removed " + remove + " matchstick(s) from row " + (i + 1), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
    }

    // Helper method to count the number of rows with the same matchsticks
    private int countRowsWithSameMatchsticks(int matchsticks) {
        int count = 0;
        for (int row : rows) {
            if (row == matchsticks) {
                count++;
            }
        }
        return count;
    }

    // Helper method to find the other row with the same amount of matchsticks
    private int findOtherRowWithSameMatchsticks(int matchsticks) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == matchsticks) {
                return i;
            }
        }
        return -1; // Return -1 if no other row with the same matchsticks is found
    }


    private void updateUI() {
        for (int i = 0; i < matchstickImages.length; i++) {
            for (int j = 0; j < matchstickImages[i].length; j++) {
                if (j < rows[i]) {
                    matchstickImages[i][j].setVisibility(View.VISIBLE);
                } else {
                    matchstickImages[i][j].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void checkGameOver() {
        int total = Arrays.stream(rows).sum();
        if (total == 0) {
            // The game is over
            if (statusTextView.getText().toString().startsWith("AI's turn")) {
                statusTextView.setText("Game Over! You win!");
            } else {
                statusTextView.setText("Game Over! AI wins!");
            }
            makeMoveButton.setEnabled(false);
        }
    }

    private void resetGame() {
        rows = new int[]{1, 3, 5, 7};
        updateUI();
        statusTextView.setText("Game Status");
        makeMoveButton.setEnabled(true);
    }
}
