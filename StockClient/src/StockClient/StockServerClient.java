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

	public void run() {
		try {
			System.out.println("Verificando ações disponíveis...");
			String[] stockSymbols = stockServer.getStockSymbols();

			System.out.println("[StockSymbols]Símbolos recuperados!");
			for (int i = 0; i < stockSymbols.length; i++) {
				System.out.println(stockSymbols[i] + " : " + stockServer.getStockValue(stockSymbols[i]));
			}

			System.out.println("Comprando Ações...");
			for(int i = 0; i < stockSymbols.length; i++){
				stockExchange.buyStock(stockSymbols[i]);
			}

			System.out.println("[ValueType] Verificando ações atualizadas");
			StockInfo[] stockInfoList = stockServer.getStockInfoList();
			for(StockInfo si : stockInfoList){
				System.out.println(si._toString());
			}

		} catch (UnknownSymbol unknownSymbol) {
			unknownSymbol.printStackTrace();
		}
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
