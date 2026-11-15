package com.sds.bookrecommendation.entity;

/**
 * The kinds of user actions we log: opening a book page, clicking a card, saving a book.
 */
public enum InteractionType {
    VIEW,
    CLICK,
    SAVE
}