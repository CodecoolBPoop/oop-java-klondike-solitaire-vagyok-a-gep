package com.codecool.klondike;

public enum Tableau {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

    Tableau(int tableauNumber) {
        this.tableauNumber = tableauNumber;
    }

    private int tableauNumber;

    public int getTableauNumber() {return tableauNumber;}
}
