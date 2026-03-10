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

        String[] suits = {"♠", "♥", "♦", "♣"};
        Color[] suitColors = {Color.BLACK, Color.RED, Color.RED, Color.BLACK};

        for (int i = 0; i < 4; i++) {
            int value = random.nextInt(13) + 1; // 1-13
            int suitIndex = random.nextInt(4);
            currentCardValues.add(value);
            cardBox.getChildren().add(createCardVisual(value, suits[suitIndex], suitColors[suitIndex]));
        }
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

    private StackPane createCardVisual(int value, String suit, Color color) {
        String face = switch (value) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> String.valueOf(value);
        };

        Rectangle bg = new Rectangle(80, 110);
        bg.getStyleClass().add("card-bg"); // Links to CSS

        Text text = new Text(face + "\n" + suit);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        text.setFill(color);
        text.setStyle("-fx-text-alignment: center;");

        return new StackPane(bg, text);
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
        // Recursive descent parser for math
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