package StockLogger;

import StockMarket.*;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.TRANSIENT;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by Yang on 24/06/2017.
 */
public class StockLoggerMain {

    public static void main(String[] args){

        Path iorDir = Paths.get(System.getProperty("user.dir").replaceFirst("StockLogger","StockSeller"));
        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        ORB orb = ORB.init(args, orbProps);

        ExchangePrinterPOATie displayPrinter = new ExchangePrinterPOATie(new DisplayExchangePrinter());
        ExchangePrinterPOATie filePrinter = new ExchangePrinterPOATie(new FileExchangePrinter());



    }

}
