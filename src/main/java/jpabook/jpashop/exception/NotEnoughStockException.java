package jpabook.jpashop.exception;

public class NotEnoughStockException extends RuntimeException{

    /* RuntimeException을 상속하여 메소드들을 오버라이드하면 쉽게 Exception을 구현할 수 있다. */
    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

    protected NotEnoughStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
