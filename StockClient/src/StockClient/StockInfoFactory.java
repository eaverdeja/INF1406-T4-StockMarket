package StockClient;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

import java.io.Serializable;

/**
 * Created by Yang on 21/06/2017.
 */
public class StockInfoFactory implements ValueFactory {
    @Override
    public Serializable read_value(InputStream inputStream) {
        return inputStream.read_value(new StockInfoImpl());
    }
}
