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
    private static DisplayExchangePrinter displayPrinter;
    private static FileExchangePrinter filePrinter;

    public static void main(String[] args) {

        Path iorDir = Paths.get(System.getProperty("user.dir").replaceFirst("StockLogger", "StockSeller"));
        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        orb = ORB.init(args, orbProps);

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
            System.err.println("[FileNotFoundException - exportServer]: "+e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("[IOException - exportServer]: "+e.getMessage());
            System.exit(1);
        }
    }

    public static void exportServer() {
        try {
            //Criamos o POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();


            displayPrinter = new DisplayExchangePrinter();
            filePrinter = new FileExchangePrinter();

            org.omg.CORBA.Object objDisplayPrinter = poa.servant_to_reference(displayPrinter);
            org.omg.CORBA.Object objFilePrinter = poa.servant_to_reference(filePrinter);

            //Criamos o arquivo IOR de Display e File ExchangePrinters
            PrintWriter ps = new PrintWriter(new FileOutputStream(
                    new File("DisplayExchangePrinter.ior")
            ));
            ps.println(orb.object_to_string(objDisplayPrinter));
            ps.close();

            ps = new PrintWriter(new FileOutputStream(
                    new File("FileExchangePrinter.ior")
            ));
            ps.println(orb.object_to_string(objFilePrinter));
            ps.close();

            exchange.connectPrinter( ExchangePrinterHelper.narrow(objDisplayPrinter));
            exchange.connectPrinter( ExchangePrinterHelper.narrow(objFilePrinter));

            orb.run();

        } catch (InvalidName invalidName) {
            System.err.println("[InvalidName - exportServer]: "+invalidName.getMessage());
            System.exit(1);
        } catch (ServantNotActive servantNotActive) {
            System.err.println("[ServantNotActive - exportServer]: "+ servantNotActive.getMessage());
            System.exit(1);
        } catch (WrongPolicy wrongPolicy) {
            System.err.println("[WrongPolicy - exportServer]: "+wrongPolicy.getMessage());
            System.exit(1);
        } catch (AdapterInactive adapterInactive) {
            System.err.println("[WrongPolicy - exportServer]: "+adapterInactive.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println("[FileNotFoundException - exportServer]: "+e.getMessage());
            System.exit(1);
        }
    }
}
