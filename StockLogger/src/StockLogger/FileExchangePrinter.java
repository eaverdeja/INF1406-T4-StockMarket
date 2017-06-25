package StockLogger;

import StockMarket.ExchangePrinterPOA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Yang on 24/06/2017.
 */
public class FileExchangePrinter extends ExchangePrinterPOA {

    private final static Object MUTEX = new Object();
    private final String nameFile = "StockLogging.txt";
    private static BufferedWriter bw;

    public FileExchangePrinter(){
        super();
    }

    @Override
    public void print(String symbol) {
        synchronized (MUTEX){
            try {
                bw = new BufferedWriter(new FileWriter(nameFile,true));
                bw.write(symbol);
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                System.err.println("Erro ao escrever no arquivo!");
            }
        }
    }
}
