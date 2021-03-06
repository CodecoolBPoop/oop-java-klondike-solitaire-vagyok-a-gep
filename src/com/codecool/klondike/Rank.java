package com.codecool.klondike;

public enum Rank {

    ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6),
    SEVEN(7), EIGHT(8), NINE(9), TEN(10), JUMBO(11), QUEEN(12), KING(13);

    Rank(int rankNumber) {
        this.rankNumber = rankNumber;
    }

    private int rankNumber;

    public int getRankNumber() {return rankNumber;}

}
