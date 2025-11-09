package ru.promo_it.linkshortener.exception;

public class LimitExceededException extends RuntimeException { public LimitExceededException() { super("Лимит переходов исчерпан."); } }
