package StockSeller;

import StockMarket.*;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Yang on 24/06/2017.
 */
public class StockSellerTieImpl implements StockServerOperations,StockExchangeOperations {

    private final Object mutex = new Object();
    private final Object printerMutex = new Object();
    private ArrayList<StockInfo> stockInfoList;
    private ConcurrentHashMap<ExchangePrinter, Integer> exchangePrinters;
    private static ORB orb;

    public StockSellerTieImpl(ORB orb) {
        stockInfoList = new ArrayList<StockInfo>();
        exchangePrinters = new ConcurrentHashMap<>();

        StockSellerTieImpl.orb = orb;
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
        for(int i = 0; i < stockInfoList.size(); i++){
            if(stockInfoList.get(i).name.equals(symbol)){
                synchronized (mutex) {
                    stockInfoList.get(i).value = stockInfoList.get(i).value + (stockInfoList.get(i).value * 0.1F);

                    activatePrinters(stockInfoList.get(i).name);
                }
                return true;
            }
        }
        //Lança exceção caso simbolo seja desconhecido
        //retorno de false é inalcançavel.
        throw new UnknownSymbol();
    }

    private void activatePrinters(String stockSymbol) {
        Iterator<Map.Entry<ExchangePrinter, Integer>> iterator = exchangePrinters.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<ExchangePrinter, Integer> entry = iterator.next();
            ExchangePrinter printer = entry.getKey();

            try {
                synchronized (printerMutex) {
                    printer.print(stockSymbol);
                }
            } catch (TRANSIENT e) {
                System.err.println("O serviço encontra-se indisponível");
                //Incrementamos o contador de tentativas
                exchangePrinters.put(printer, entry.getValue() + 1);
                //Após uma tentativa, removemos a impressora da lista
                System.err.println("Retries of "+ entry.getKey()+" : "+ entry.getValue());
                if(entry.getValue() >= 1) {
                    System.out.println(exchangePrinters.remove(printer));
                    System.err.println("Impressora com falha de comunicação removida");
                }
            } catch (COMM_FAILURE e1) {
                System.err.println("Falha de comunicação com o serviço");
                exchangePrinters.remove(entry);
                System.err.println("Impressora com falha de comunicação removida");
            }
        }
    }

    @Override
    public boolean connectPrinter(ExchangePrinter printer) {
        //Registramos a existencia de uma impressora
        this.exchangePrinters.put(printer, 0);
        System.out.println("Nova ExchangePrinter cadastrada: "+printer);
        return true;
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

    private static ExchangePrinter getPrinter() {
        //Buscamos as impressoras
        Path iorDir = Paths.get(System.getProperty("user.dir").replaceFirst("StockSeller","StockLogger"));
        ExchangePrinter printer = null;
        try {
            //Recuperamos a referencia para o objeto StockLoger do StockLogger.ior
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(iorDir.toString().concat("/StockLogger.ior"))));
            String iorLogger = reader.readLine();
            org.omg.CORBA.Object objStockLogger = orb.string_to_object(iorLogger);
            printer = ExchangePrinterHelper.narrow(objStockLogger);
        } catch (FileNotFoundException e) {
            System.err.println("Arquivo inexistente:\n"+e);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo:\n"+e);
        } catch (TRANSIENT e) {
            System.err.println("O serviço encontra-se indisponível");
        } catch (COMM_FAILURE e) {
            System.err.println("Falha de comunicação com o serviço");
        }

        return printer;
    }
}
