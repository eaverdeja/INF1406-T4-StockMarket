package StockClient;

import StockMarket.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
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

	private boolean buyStock(){

        StockInfo[] stockInfoList = stockServer.getStockInfoList();
		System.out.println("--------------");
		System.out.println("Ações disponiveis:");
		System.out.println("Numero | Ação | Valor");
        int i = 0;
        for(StockInfo si : stockInfoList){
            System.out.println(i+" | "+stockInfoList[i].name+" | "+stockInfoList[i].value);
            i++;
        }

        int symbolNum;
		do {
            System.out.println("Digite o número de uma das ações acima que deseja comprar: ");
            Scanner scanner = new Scanner(System.in);

			symbolNum = -1;
            try {
            	if(scanner.hasNextInt()) {
					symbolNum = scanner.nextInt();
				} else if(scanner.hasNext("exit")) {
            		return false;
				}
			} catch (InputMismatchException inputMismatch) {
				System.err.println("Comando desconhecido! " + inputMismatch.getCause());
			}
        } while( symbolNum > (stockInfoList.length-1) || symbolNum == -1);

        try {
            stockExchange.buyStock(stockInfoList[symbolNum].name);
            System.out.println("Valor atualizado da ação: "+stockInfoList[symbolNum].name+": "
                +stockServer.getStockValue(stockInfoList[symbolNum].name));
        } catch (UnknownSymbol unknownSymbol) {
            System.err.println("Simbolo da Ação desconhecido");
        }

        return true;
    }


	public void run() {
	    String opt = null;
	    do{
	        if(!buyStock()) {
	        	break;
			}
	        System.out.print("Digite 'exit' para sair. Qualquer outro comando imprimirá a lista de ações novamente.");
            Scanner scanner = new Scanner(System.in);
            opt = scanner.next();
        } while (!opt.equals("exit"));
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
			System.err.println("[FileNotFoundException] Arquivo inexistente:\n"+e);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("[IOException] Erro ao ler arquivo:\n"+e);
			System.exit(1);
		} catch (TRANSIENT e) {
			System.err.println("[TRANSIENT] O serviço encontra-se indisponível");
		} catch (COMM_FAILURE e) {
			System.err.println("[COMM_FAILURE] Falha de comunicação com o serviço");
			System.exit(1);
		}

	}

}
