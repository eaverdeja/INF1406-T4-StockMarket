package StockSeller;

import StockMarket.StockExchangePOATie;
import StockMarket.StockServerPOATie;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by Yang on 24/06/2017.
 */
public class StockSellerMain {

    public static void main(String[] args) {

        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        ORB orb = ORB.init(args, orbProps);

        //Geramos a instancia do Servant do StockSeller
        StockSellerTieImpl stockSeller = new StockSellerTieImpl();

        //Como o servant precisa implementar duas interfaces de StockMarket
        // utilizamos a estratégia por Delegação. Abaixo criamos os objetos
        // POATie das respectivas interfaces.
        StockServerPOATie stockServer = new StockServerPOATie(stockSeller);
        StockExchangePOATie stockExchange =  new StockExchangePOATie(stockSeller);

        try {
            //Criamos o POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            //Exportamos o servant de StockServer
            org.omg.CORBA.Object objStockServer = poa.servant_to_reference(stockServer);
            //Criamos o arquivo IOR de StockServer
            PrintWriter ps = new PrintWriter(new FileOutputStream(
                    new File("StockServer.ior")
            ));
            ps.println(orb.object_to_string(objStockServer));
            ps.close();

            //Exportamos o servant de StockExchange
            org.omg.CORBA.Object objStockExchange = poa.servant_to_reference( stockExchange);
            //Criamos o arquivo IOR de StockExchange
            ps = new PrintWriter(new FileOutputStream(
                    new File("StockExchange.ior")
            ));
            ps.println(orb.object_to_string(objStockExchange));
            ps.close();


            orb.run();
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
