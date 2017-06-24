package StockSeller;

import StockMarket.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Yang on 24/06/2017.
 */
public class StockSellerTieImpl implements StockServerOperations,StockExchangeOperations {

    private final Object mutex = new Object();
    private ArrayList<StockInfo> stockInfoList;

    public StockSellerTieImpl(){
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
    public boolean buyStock(String symbol) throws UnknownSymbol {
        //TODO notificar impressoras conhecidas;
        //TODO tratar erros de comunicação imprimindo na tela

        for(int i = 0; i < stockInfoList.size(); i++){
            if(stockInfoList.get(i).name.equals(symbol)){
                synchronized (mutex) {
                    stockInfoList.get(i).value = stockInfoList.get(i).value + (stockInfoList.get(i).value * 0.1F);
                    return true;
                }
            }
        }

        //Lança exceção caso simbolo seja desconhecido
        //retorno de false é inalcançavel.
        throw new UnknownSymbol();
    }

    @Override
    public boolean connectPrinter(ExchangePrinter printer) {
        //TODO registrar existencia de uma impressora
        return false;
    }

    @Override
    public float getStockValue(String symbol) throws UnknownSymbol {
        synchronized (mutex) {
            for(StockInfo si : stockInfoList ) {
                if (si.name.equals(symbol)){
                    return si.value;
                }
            }
            return 0;
        }
    }

    @Override
    public String[] getStockSymbols() {
        //TODO faz sentido fazer isso toda vez?
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
