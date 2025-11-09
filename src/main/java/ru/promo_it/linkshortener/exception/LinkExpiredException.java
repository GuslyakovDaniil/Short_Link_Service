package ru.promo_it.linkshortener.exception;

public class LinkExpiredException extends RuntimeException { public LinkExpiredException() { super("Срок жизни ссылки истек."); } }
