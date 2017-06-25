package StockClient;

import StockMarket.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;

public class StockServerClient {
	private StockServer stockServer;
	private StockExchange stockExchange;

	public StockServerClient(StockServer stockServer,StockExchange stockExchange) {
		this.stockServer = stockServer;
		this.stockExchange = stockExchange;
	}

	private void buyStock(){

	    System.out.println("Ações disponiveis:");

        StockInfo[] stockInfoList = stockServer.getStockInfoList();
        System.out.println("Numero | Ação | Valor");
        int i = 0;
        for(StockInfo si : stockInfoList){
            System.out.println(i+" | "+stockInfoList[i].name+" | "+stockInfoList[i].value);
            i++;
        }
        int symbolNum;
        do {
            System.out.print("Digite o número de uma das ações acima que deseja comprar: ");
            Scanner scanner = new Scanner(System.in);
            symbolNum = scanner.nextInt();
        }while(symbolNum < 0 || symbolNum > (stockInfoList.length-1));
        try {
            stockExchange.buyStock(stockInfoList[symbolNum].name);
            System.out.println("Valor atualizado da ação: "+stockInfoList[symbolNum].name+": "
                +stockServer.getStockValue(stockInfoList[symbolNum].name));
        } catch (UnknownSymbol unknownSymbol) {
            System.err.println("Simbolo da Ação desconhecido");
        }

    }


	public void run() {
	    int opt;
	    do{
	        buyStock();
	        System.out.print("Digite '0' (zero) para sair");
            Scanner scanner = new Scanner(System.in);
            opt = scanner.nextInt();
        } while (opt != 0);
	    System.out.println("Terminando StockClient...");
	}

	public static void main(String[] args) {

		Path iorDir = Paths.get(System.getProperty("user.dir").replaceFirst("StockClient","StockSeller"));
		Properties orbProps = new Properties();
		orbProps.setProperty("org.omg.CORBA.ORBClass","org.jacorb.orb.ORB");
		orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
		ORB orb = ORB.init(args, orbProps);

		try {
			//Recuperamos a referencia para o objeto StockServer do StockServer.ior
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(iorDir.toString().concat("/StockServer.ior"))));
			String iorServer = reader.readLine();
			org.omg.CORBA.Object objStockServer = orb.string_to_object(iorServer);
			StockServer server = StockServerHelper.narrow(objStockServer);

			//Recuperamos a referencia para o objeto StockExchange do StockExchange.ior
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(iorDir.toString().concat("/StockExchange.ior"))));
			String iorExchange = reader.readLine();
			org.omg.CORBA.Object objStockExchange = orb.string_to_object(iorExchange);
			StockExchange exchange = StockExchangeHelper.narrow(objStockExchange);

			//Iniciamos o cliente
			StockServerClient stockClient = new StockServerClient(server,exchange);

			//
			orb.register_value_factory(StockInfoHelper.id(),new StockInfoFactory());

			stockClient.run();

		} catch (FileNotFoundException e) {
			System.err.println("Arquivo inexistente:\n"+e);
		} catch (IOException e) {
			System.err.println("Erro ao ler arquivo:\n"+e);
		} catch (TRANSIENT e) {
			System.err.println("O serviço encontra-se indisponível");
		} catch (COMM_FAILURE e) {
			System.err.println("Falha de comunicação com o serviço");
		}

	}

}
