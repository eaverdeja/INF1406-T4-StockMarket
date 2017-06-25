package StockLogger;

import StockMarket.ExchangePrinterPOA;

/**
 * Created by Yang on 24/06/2017.
 */
public class DisplayExchangePrinter extends ExchangePrinterPOA{

    public DisplayExchangePrinter(){
        super();
    }

    @Override
    public void print(String symbol) {
        System.out.println(symbol);
        return;
    }
}
