package com.codecool.klondike;

public enum Suit {
    CLUBS("clubs"), DIAMONDS("diamonds"), HEARTS("hearts"), SPADES("spades");

    public String getName() {
        return suitName;
    }
    private String suitName;

    Suit(String suitName) {
        this.suitName = suitName;
    }
}
