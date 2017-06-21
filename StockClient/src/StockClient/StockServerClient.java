package StockClient;

import StockMarket.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;

public class StockServerClient {
	private StockServer myStock;

	public StockServerClient(StockServer stockServer) {
		this.myStock = stockServer;
	}

	public void run() {
		try {
			String[] stockSymbols = myStock.getStockSymbols();

			//valuetype
			StockInfo[] stockInfoList = myStock.getStockInfoList();

			System.out.println("Símbolos recuperados!");
			for (int i = 0; i < stockSymbols.length; i++) {
				System.out.println(stockSymbols[i] + " : " + myStock.getStockValue(stockSymbols[i]));
			}

			System.out.println("[ValueType] Símbolos recuperados!");
			for (int i = 0; i < stockInfoList.length; i++) {
				System.out.println(stockInfoList[i]._toString());
			}
		} catch (UnknownSymbol unknownSymbol) {
			unknownSymbol.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		Properties orbProps = new Properties();
		
		orbProps.setProperty("org.omg.CORBA.ORBClass","org.jacorb.orb.ORB");
		orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
		ORB orb = ORB.init(args, orbProps);

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			String ior = reader.readLine();

			org.omg.CORBA.Object obj = orb.string_to_object(ior);
			StockServer server = StockServerHelper.narrow(obj);

			StockServerClient stockClient = new StockServerClient(server);

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
