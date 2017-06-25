package StockLogger;

import StockMarket.ExchangePrinter;
import StockMarket.ExchangePrinterOperations;

/**
 * Created by Yang on 24/06/2017.
 */
public class DisplayExchangePrinter implements ExchangePrinterOperations {
    @Override
    public void print(String symbol) {
        System.out.println(symbol);
        return;
    }
}
