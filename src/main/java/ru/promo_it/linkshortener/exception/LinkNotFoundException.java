package ru.promo_it.linkshortener.exception;

public class LinkNotFoundException extends RuntimeException { public LinkNotFoundException() { super("Ссылка не найдена."); } }
