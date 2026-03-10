package com.example.cardgame;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardGameController {

    @FXML private HBox cardBox;
    @FXML private TextField expressionField;

    private final List<Integer> currentCardValues = new ArrayList<>();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        handleRefresh();
    }

    @FXML
    public void handleRefresh() {
        cardBox.getChildren().clear();
        currentCardValues.clear();
        expressionField.clear();

        // Use words so they match the .png file names
        String[] suits = {"spades", "hearts", "diamonds", "clubs"};

        for (int i = 0; i < 4; i++) {
            int value = random.nextInt(13) + 1; // Generates 1 to 13
            int suitIndex = random.nextInt(4);

            currentCardValues.add(value);

            // Call our new image method!
            cardBox.getChildren().add(createCardImageView(value, suits[suitIndex]));
        }
    }

    private StackPane createCardImageView(int value, String suit) {
        // Convert numbers 1, 11, 12, 13 into their face names for the file path
        String valueName = switch (value) {
            case 1 -> "ace";
            case 11 -> "jack";
            case 12 -> "queen";
            case 13 -> "king";
            default -> String.valueOf(value); // Keeps 2 through 10 as numbers
        };

        String fileName = valueName + "_of_" + suit + ".png";

        String imagePath = "/com/example/cardgame/png/" + fileName;

        // Loads image from the resources folder
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);

        // Scales the image down
        imageView.setFitWidth(90);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(true);

        StackPane cardWrapper = new StackPane(imageView);
        cardWrapper.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-color: white; -fx-background-radius: 4;");

        cardWrapper.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        return cardWrapper;
    }

    @FXML
    public void handleVerify() {
        String expression = expressionField.getText();
        if (expression == null || expression.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter an expression.");
            return;
        }

        // Checking that exact numbers are used
        List<Integer> extractedNumbers = extractNumbers(expression);
        List<Integer> sortedCards = new ArrayList<>(currentCardValues);
        Collections.sort(extractedNumbers);
        Collections.sort(sortedCards);

        if (!extractedNumbers.equals(sortedCards)) {
            showAlert(Alert.AlertType.ERROR, "Incorrect Numbers", "You must use exactly the four numbers shown on the cards.");
            return;
        }

        // Checks math
        try {
            double result = evaluateMath(expression);
            if (Math.abs(result - 24.0) < 0.0001) {
                showAlert(Alert.AlertType.INFORMATION, "Success!", "Correct! The expression equals 24.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Incorrect!", "The expression equals " + result + ", not 24.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Syntax Error", "The expression is invalid.");
        }
    }


    private List<Integer> extractNumbers(String expr) {
        List<Integer> nums = new ArrayList<>();
        // Treats face card based on their integer value
        Matcher m = Pattern.compile("\\d+").matcher(expr);
        while (m.find()) {
            nums.add(Integer.parseInt(m.group()));
        }
        return nums;
    }

    private double evaluateMath(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }
        }.parse();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}