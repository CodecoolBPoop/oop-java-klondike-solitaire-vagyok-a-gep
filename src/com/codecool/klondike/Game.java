package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();


    private double dragStartX, dragStartY;

    public List<Card> getDraggedCards() {
        return draggedCards;
    }

    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK
                && card.getContainingPile().getTopCard() == card) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown())
            return;
        if (activePile.getPileType() == Pile.PileType.DISCARD && card != discardPile.getTopCard())
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();

        for (Card currentCard : getSelectedCards(card, activePile)) {
            draggedCards.add(currentCard);

            currentCard.toFront();
            currentCard.setTranslateX(offsetX);
            currentCard.setTranslateY(offsetY);
        }
        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        //TODO
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);

        if ((pile) != null) {
            handleValidMove(card, pile);
        } else if (foundationPile != null) {
            handleValidMove(card, foundationPile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        int discardSize = discardPile.getCards().size();
        if (stockPile.isEmpty()) {
            for (int i = 0; i < discardSize; i++) {
                discardPile.getTopCard().moveToPile(stockPile);
                stockPile.getTopCard().flip();
            }
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {

        if (destPile.getPileType() == Pile.PileType.TABLEAU && destPile.isEmpty() && card.getRank() == Rank.KING) {
            turnUpTopCard(card);
            return true;
        }

        if ((!destPile.isEmpty()) && (destPile.getPileType() == Pile.PileType.TABLEAU)) {
            if (Card.isOppositeColor(card, destPile.getTopCard()) && isUnderCard(card, destPile)) {
                System.out.println("VALID");
                turnUpTopCard(card);
                return true;
            }
        }

        if ((!destPile.isEmpty()) && destPile.getPileType() == Pile.PileType.FOUNDATION && isUnderCardFound(destPile) &&
                Card.isSameSuit(destPile.getTopCard(), card)) {
            turnUpTopCard(card);
            return true;
        }


        if (destPile.getPileType() == Pile.PileType.FOUNDATION && destPile.isEmpty() && card.getRank() == Rank.ACE) {
            System.out.println("good");
            turnUpTopCard(card);
            return true;
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
        System.out.println("not valid");
        return false;

    }


    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {

            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
        for (int i = 0; i < tableauPiles.size(); i++) {

            Pile pile = tableauPiles.get(i);
            for (int j = 0; j < i + 1; j++) {
                stockPile.getTopCard().moveToPile(pile);
                if (i == j) {
                    pile.getTopCard().flip();
                }
            }
        }
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public boolean isUnderCard(Card card, Pile pile) {
        Rank cardNumber = card.getRank();
        int cardNumberInInt = cardNumber.getRankNumber();

        Card topCard = pile.getTopCard();
        Rank topCardNumber = topCard.getRank();
        int topCardNumberInInt = topCardNumber.getRankNumber();

        if (cardNumberInInt + 1 == topCardNumberInInt) {
            return true;
        } else {
            return false;
        }
    }

    public void turnUpTopCard(Card card) {

        Pile starterPile = card.getContainingPile();
        ObservableList<Card> cards = starterPile.getCards();
        if (cards.size() != 1) {
            if (cards.size() - draggedCards.size() >= 1) {
                if ((cards.size() > 0) && cards.get(cards.size() - draggedCards.size() - 1).isFaceDown()) {
                    cards.get(cards.size() - draggedCards.size() - 1).flip();
                }
            }
        } else if (cards.get(0).isFaceDown()){
            cards.get(0).flip();
        }
    }

    public boolean isUnderCardFoundation(Rank card1, Rank card2) {

        return card1.ordinal() + 1 == card2.ordinal();
    }

    public boolean isUnderCardFound(Pile pile) {
        if (draggedCards.get(0).getRank().getRankNumber() - 1 == pile.getTopCard().getRank().getRankNumber()) {
            return true;
        }
        return false;
    }

    public static List<Card> getSelectedCards( Card currentCard, Pile activePile) {

        List<Card> selectedCards = new ArrayList<>();

        int i = activePile.getCards().indexOf(currentCard);
        for( int j=i; j < activePile.getCards().size(); j++) {
            selectedCards.add( activePile.getCards().get(j));
        }

        return selectedCards;
    }
}
