package StockClient;

import StockMarket.*;

/**
 * Created by Yang on 21/06/2017.
 */
public class StockInfoImpl extends StockInfo {


    public  StockInfoImpl(){
        super();
    }

    public StockInfoImpl(String name, Float value){
        super();
        this.name = name;
        this.value = value;
    }

    @Override
    public String _toString() {
        return this.name+": "+this.value;
    }
}
