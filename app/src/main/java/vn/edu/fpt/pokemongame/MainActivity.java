package vn.edu.fpt.pokemongame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private GridLayout gameBoard;
    private int cellSideSize;
    private Bitmap[] images;
    private ImageView pairingImageView;
    private LineDrawView lineCanvas;
    private static final int WIDTH = 16;
    private static final int HEIGHT = 9;
    private ImageButton hintBtn;
    private ImageButton shuffleBtn;
    private ProgressBar progressBar;
    private ImageView background;
    private TextView tvHintRemaining;
    private static int hintRemaining = 3;
    private TextView tvShuffleRemaining;
    private static int shuffleRemaining = 3;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize game
        ConstraintLayout cl = findViewById(R.id.gameView);
        gameBoard = findViewById(R.id.gameBoard);
        lineCanvas = findViewById(R.id.lineCanvas);
        hintBtn = findViewById(R.id.hintBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        background = findViewById(R.id.background);
        progressBar = findViewById(R.id.gradientProgressBar);
        tvHintRemaining = findViewById(R.id.tvHintRemaining);
        tvShuffleRemaining = findViewById(R.id.tvShuffleRemaining);
        images = new Bitmap[GameManager.TILES];

        //get hint and shuffle from previous level (default 3)
        hintRemaining = getIntent().getIntExtra("hint", 3);
        shuffleRemaining = getIntent().getIntExtra("shuffle", 3);
        tvHintRemaining.setText(hintRemaining + "");
        tvShuffleRemaining.setText(shuffleRemaining + "");

        //get image files with the name "piecesX.png"
        AssetManager assetManager = getAssets();
        ArrayList<String> piecesFiles = new ArrayList<>();
        Pattern pattern = Pattern.compile("pieces(\\d+)\\.png");
        try {
            String[] files = assetManager.list("");
            for (String file : files) {
                if (pattern.matcher(file).matches()) {
                    piecesFiles.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sort the file names LEXICOGRAPHICALLY (1,2,3...)
        piecesFiles.sort((o1, o2) -> {
            Matcher m1 = pattern.matcher(o1);
            Matcher m2 = pattern.matcher(o2);
            if (m1.matches() && m2.matches()) {
                int n1 = Integer.parseInt(m1.group(1)); // m1.group is the number after "pieces"
                int n2 = Integer.parseInt(m2.group(1)); // m2.group is the number after "pieces"
                return n1 - n2; //sort by these two numbers.
            }
            return o1.compareTo(o2); //in case it doesn't have number, sort it by alphabetically by default.
        });

        //load and store image files into bitmap array
        int imagesIndex = 0;
        for (String file : piecesFiles) {
            try {
                InputStream is = assetManager.open(file);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();

                images[imagesIndex] = bitmap;
                imagesIndex++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //set shuffle and hint button listener
        shuffleBtn.setOnClickListener(v -> {
            if (shuffleRemaining > 0) {
                populateCardsAgain(true);
                shuffleRemaining--;
                tvShuffleRemaining.setText(shuffleRemaining + "");
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.no_shuffle_left), Toast.LENGTH_SHORT).show();
            }
        });
        hintBtn.setOnClickListener(v -> {
            if (hintRemaining > 0) {
                showHint();
                hintRemaining--;
                tvHintRemaining.setText(hintRemaining + "");
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.no_hint_left), Toast.LENGTH_SHORT).show();
            }
        });

        //set the timer for 10 minutes, with each tick for 1 seconds
        final int totalSeconds = 600;
        progressBar.setMax(totalSeconds);
        progressBar.setProgress(totalSeconds);
        countDownTimer = new CountDownTimer(totalSeconds * 1000, 1000) {
            //after each tick
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(secondsRemaining);
            }

            //when the taskbar ends its work
            public void onFinish() {
                progressBar.setProgress(0);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.game_over))
                        .setMessage(getString(R.string.time_up))
                        .setPositiveButton(getString(R.string.play_again), (di, which) -> {
                            Intent intent = new Intent(MainActivity.this, getClass());
                            intent.putExtra("hint", hintRemaining);
                            intent.putExtra("shuffle", ++shuffleRemaining);
                            finish();
                            startActivity(intent);
                        })
                        .setNegativeButton(getString(R.string.quit), (di, which) -> {
                            finish();
                        })
                        .show();
            }
        }.start();

        //after ConstraintLayout is rendered, get the height and width and divide so that the cards will fit properly.
        cl.post(() -> {
            int width = cl.getWidth(); //px
            int height = cl.getHeight(); //px
            float result1 = (float) height / (HEIGHT + 2); //2 is for the space of drawing line in case of going outside the bound
            float result2 = (float) width / (WIDTH + 2); //2 is for the space of drawing line in case of going outside the bound

            cellSideSize = (int) Math.floor(Math.min(result1, result2));
            initializeBoard();
        });
    }

    void initializeBoard() {
        GameManager.createCards(HEIGHT, WIDTH);
        GameManager.createRectangle(HEIGHT, WIDTH);
        GameManager.shuffleCards();
        GameManager.populateCardsToBoard(false);
        populateCardsAgain(false); //make sure there is always a valid move

        //create cards and put into the board
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                ImageView card = new ImageView(this);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(cellSideSize, cellSideSize); //width: xx px, height: xx px
                card.setLayoutParams(params);
                card.setTag(R.id.tag_y, i); //set each card with x coordinate
                card.setTag(R.id.tag_x, j); //set each card with y coordinate
                card.setImageBitmap(images[GameManager.cards.get(i * WIDTH + j) - 1]);

                card.setOnClickListener(v -> clickOnCard(card));
                gameBoard.addView(card);
            }
        }

        //after the board is rendered, set the canvas size and the background size
        gameBoard.post(() -> {
            int gameBoardWidth = gameBoard.getWidth();
            int gameBoardHeight = gameBoard.getHeight();
            ConstraintLayout.LayoutParams lineCanvasParams = new ConstraintLayout.LayoutParams(gameBoardWidth + cellSideSize * 2, gameBoardHeight + cellSideSize * 2);
            lineCanvasParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            lineCanvasParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lineCanvasParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            lineCanvasParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            lineCanvas.setLayoutParams(lineCanvasParams);
            background.setLayoutParams(lineCanvasParams);
        });
    }

    //reassigning the images into random positions in case of shuffle
    void reAssignImages() {
        int length = gameBoard.getChildCount();
        int cardIndex = 0;
        for (int i = 0; i < length; i++) {
            ImageView card = (ImageView) gameBoard.getChildAt(i);
            if (card.getVisibility() == View.VISIBLE) {
                card.setImageBitmap(images[GameManager.cards.get(cardIndex) - 1]);
                cardIndex++;
            }
        }
    }

    //repopulate cards if there are no valid moves left, or if user presses "Shuffle button"
    void populateCardsAgain(boolean forced) {
        if (forced) {
            GameManager.shuffleCards();
            reAssignImages();
            GameManager.populateCardsToBoard(true);
        }
        while (GameManager.validPairs.isEmpty()) {
            GameManager.shuffleCards();
            reAssignImages();
            GameManager.populateCardsToBoard(true);
        }
    }

    //set the click event to cards
    void clickOnCard(ImageView card) {
        //set the blur effect
        card.setForeground(new ColorDrawable(0x80FFFFFF));
        if (pairingImageView == null) { //1 card is selected
            pairingImageView = card;
        } else { //2 cards are selected
            //get the coordinates of each point
            int x1 = (int) pairingImageView.getTag(R.id.tag_x);
            int y1 = (int) pairingImageView.getTag(R.id.tag_y);
            int x2 = (int) card.getTag(R.id.tag_x);
            int y2 = (int) card.getTag(R.id.tag_y);

            //set the drawing start point and end point
            Point p1;
            Point p2;
            if (y2 < y1 || (y2 == y1 && x2 < x1)) {
                p2 = new Point(x1, y1);
                p1 = new Point(x2, y2);
            } else {
                p1 = new Point(x1, y1);
                p2 = new Point(x2, y2);
            }

            //if two selected cards are connected, draw line and remove card.
            boolean canConnect = GameManager.canTwoPointsConnected(p1, p2);
            if (canConnect) {
                PointPair pp = new PointPair(p1, p2);
                PointPair ppMiddleLine = GameManager.validPairs.get(pp);
                drawLine(p1, p2, ppMiddleLine);
                GameManager.removeCard(p1, p2);
            }

            card.postDelayed(() -> {
                //remove the highlighted color and the draw line
                if (pairingImageView != null) pairingImageView.setForeground(null);
                card.setForeground(null);
                drawLine(null, null, null);
                pairingImageView = null;

                if (!canConnect) {
                    return;
                }

                //sync images to board after board matrix is changed
                synchronizeBoard();
                //check if all board are empty -> you win
                if (GameManager.checkEmptyBoard()) {
                    countDownTimer.cancel();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.you_win))
                            .setMessage(getString(R.string.next_level))
                            .setPositiveButton(getString(R.string.continue_game), (di, which) -> {
                                Intent intent = new Intent(this, getClass());
                                intent.putExtra("hint", hintRemaining);
                                intent.putExtra("shuffle", ++shuffleRemaining);
                                finish();
                                startActivity(intent);
                            })
                            .setNegativeButton(getString(R.string.quit), (di, which) -> {
                                finish();
                            });
                    builder.setCancelable(false);
                    builder.show();
                } else {
                    //if there isn't any available move
                    if (GameManager.validPairs.isEmpty()) {
                        //if shuffle is remaining
                        if (shuffleRemaining > 0) {
                            shuffleBtn.performClick();
                        } else {
                            //if no more moves + no shuffle -> you lose
                            countDownTimer.cancel();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.game_over))
                                    .setMessage(getString(R.string.no_more_moves))
                                    .setPositiveButton(getString(R.string.play_again), (di, which) -> {
                                        Intent intent = new Intent(this, getClass());
                                        intent.putExtra("hint", 3);
                                        intent.putExtra("shuffle", 3);
                                        finish();
                                        startActivity(intent);
                                    })
                                    .setNegativeButton(getString(R.string.quit), (di, which) -> {
                                        finish();
                                    });
                            builder.setCancelable(false);
                            builder.show();
                        }
                    } else {
                        populateCardsAgain(false);
                    }
                }
            }, 100);
        }
    }

    //draw a line using canvas
    void drawLine(Point p1, Point p2, PointPair pp) {
        if (p1 == null && p2 == null) {
            lineCanvas.drawLine(null, null, null);
            return;
        }
        Point start = new Point(cellSideSize / 2 + (p1.x + 1) * cellSideSize, cellSideSize / 2 + (p1.y + 1) * cellSideSize);
        Point end = new Point(cellSideSize / 2 + (p2.x + 1) * cellSideSize, cellSideSize / 2 + (p2.y + 1) * cellSideSize);
        PointPair middleLine = null;
        if (pp != null) {
            Point middle1 = new Point(cellSideSize / 2 + (pp.p1.x + 1) * cellSideSize, cellSideSize / 2 + (pp.p1.y + 1) * cellSideSize);
            Point middle2 = new Point(cellSideSize / 2 + (pp.p2.x + 1) * cellSideSize, cellSideSize / 2 + (pp.p2.y + 1) * cellSideSize);
            middleLine = new PointPair(middle1, middle2);
        }
        lineCanvas.drawLine(start, end, middleLine);
    }

    //show hint if user presses "Hint button"
    void showHint() {
        //disable button when showing hint
        hintBtn.setEnabled(false);
        shuffleBtn.setEnabled(false);

        PointPair[] keySet = GameManager.validPairs.keySet().toArray(new PointPair[0]);
        Random random = new Random();
        PointPair randomPointPair = keySet[random.nextInt(keySet.length)];
        Point p1 = randomPointPair.p1;
        Point p2 = randomPointPair.p2;
        //find image view with tag
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            ImageView card = (ImageView) gameBoard.getChildAt(i);
            card.setEnabled(false);
            int x = (int) card.getTag(R.id.tag_x);
            int y = (int) card.getTag(R.id.tag_y);
            Point p = new Point(x, y);
            if (p.equals(p1) || p.equals(p2)) {
                card.setForeground(new ColorDrawable(0x80FFFF00));
                createBlinkEffect(card);
            }
            //remove highlight and enable button after 3 seconds
            card.postDelayed(() -> {
                card.setEnabled(true);
                card.setForeground(null);
                hintBtn.setEnabled(true);
                shuffleBtn.setEnabled(true);
            }, 2000);
        }
    }

    //create blink effect
    void createBlinkEffect(ImageView card) {
        for (int i = 0; i < 5; i++) {
            final int step = i;
            card.postDelayed(() -> {
                if (step % 2 == 0) {
                    card.setForeground(new ColorDrawable(0x80FFFF00)); //Color.parseColor("#80FFFF00")
                } else {
                    card.setForeground(null);
                }
            }, step * 400);
        }
    }

    //synchronize board matrix with images
    void synchronizeBoard() {
        for (int i = 0; i < GameManager.BOARD.length; i++) {
            for (int j = 0; j < GameManager.BOARD[0].length; j++) {
                int cardIndex = i * WIDTH + j;
                ImageView card = (ImageView) gameBoard.getChildAt(cardIndex);
                if (GameManager.BOARD[i][j] == 0) {
                    card.setVisibility(View.INVISIBLE);
                } else {
                    card.setVisibility(View.VISIBLE);
                    card.setImageBitmap(images[GameManager.BOARD[i][j] - 1]);
                }
            }
        }
    }
}