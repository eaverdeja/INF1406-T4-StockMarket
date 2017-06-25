package StockLogger;

import StockMarket.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by Yang on 24/06/2017.
 */
public class StockLoggerClient {
    private static ORB orb;
    private static StockExchange exchange ;
    private static ExchangePrinterPOATie displayPrinter;
    private static ExchangePrinterPOATie filePrinter;

    public static void main(String[] args) {

        Path iorDir = Paths.get(System.getProperty("user.dir").replaceFirst("StockLogger", "StockSeller"));
        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        orb = ORB.init(args, orbProps);

        displayPrinter = new ExchangePrinterPOATie(new DisplayExchangePrinter());
        filePrinter = new ExchangePrinterPOATie(new FileExchangePrinter());

        try {
            //Recuperamos a referencia para o objeto StockExchange do StockExchange.ior
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(iorDir.toString().concat("/StockExchange.ior"))));
            String iorExchange = reader.readLine();
            org.omg.CORBA.Object objStockExchange = orb.string_to_object(iorExchange);
            exchange = StockExchangeHelper.narrow(objStockExchange);

            //Exportamos o objeto remoto e criamos o ior
            exportServer();

            orb.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportServer() {
        try {
            //Criamos o POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            //Exportamos o servant de StockLogger
            org.omg.CORBA.Object objDisplayPrinter = poa.servant_to_reference(displayPrinter);
            org.omg.CORBA.Object objFilePrinter = poa.servant_to_reference(filePrinter);
            //Conectamos as impressoras ao StockExchange
            exchange.connectPrinter((ExchangePrinter)objDisplayPrinter);
            //exchange.connectPrinter((ExchangePrinter)objFilePrinter);
            //Criamos o arquivo IOR de StockLogger
            PrintWriter ps = new PrintWriter(new FileOutputStream(
                    new File("StockLogger.ior")
            ));
            ps.println(orb.object_to_string(objDisplayPrinter));
            ps.close();

        } catch (InvalidName invalidName) {
            System.err.println(invalidName.getMessage());
            invalidName.printStackTrace();
        } catch (ServantNotActive servantNotActive) {
            System.err.println(servantNotActive.getMessage());
            servantNotActive.printStackTrace();
        } catch (WrongPolicy wrongPolicy) {
            System.err.println(wrongPolicy.getMessage());
            wrongPolicy.printStackTrace();
        } catch (AdapterInactive adapterInactive) {
            System.err.println(adapterInactive.getMessage());
            adapterInactive.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
