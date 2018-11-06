package com.codecool.klondike;

public enum Suit {
    SPADES(3), CLUBS(4), HEARTS(1), DIAMONDS(2);
    int value;

    Suit(int value) {
        this.value = value;
    }
}
