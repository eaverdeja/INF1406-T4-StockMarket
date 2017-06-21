package StockSeller;

import StockMarket.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by eaverdeja on 19/06/17.
 */
public class StockServerImpl extends StockServerPOA {
    private final Object mutex = new Object();
    private ArrayList<StockInfo> stockInfoList;

    public StockServerImpl() {
        stockInfoList = new ArrayList<StockInfo>();
        try {
            BufferedReader stocksFile = new BufferedReader(new FileReader("StocksFile"));
            String line;
            try {
                while((line = stocksFile.readLine())!=null){
                    StockInfo stockInfo = new StockInfoImpl(line.split(" ")[0],Float.parseFloat(line.split(" ")[1]));
                    stockInfoList.add(stockInfo);
                }
            } catch (IOException e) {
                System.err.println("Erro de leitura");
            }
        } catch (FileNotFoundException e) {
            System.err.print("Arquivo não encontrado");
            System.exit(1);
        }
        System.out.println("Ações do Mercado disponibilizadas a partir de StocksFile");
        stockInfoList.forEach(s->System.out.println(s._toString()));
    }

    @Override
    public float getStockValue(String symbol) throws UnknownSymbol {
        synchronized (mutex) {
           for(StockInfo si : stockInfoList ) {
               if (si.name.equals(symbol)){
                   return si.value;
               }
           }
           return 0f;
        }
    }

    @Override
    public String[] getStockSymbols() {
        //TODO faz sentido fazer isso toda vez?s
            String[] stockSymbols = new String[stockInfoList.size()];
            for(int i=0; i < stockInfoList.size(); i++){
                stockSymbols[i] = stockInfoList.get(i).name;
            }
            return stockSymbols;
    }

    @Override
    public StockInfo[] getStockInfoList() {
        StockInfo[] stocks = new StockInfo[stockInfoList.size()];

        int i = 0;
        for(StockInfo si : stockInfoList) {
            stocks[i] = StockInfo.class.cast(si);
            i++;
        }

        return stocks;
    }
}
