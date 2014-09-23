package com.company;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

interface TwoArgInterface {

    public int operation(int a, int b);
}

/**
 * Created by Sp0x on 9/24/2014.
 */
public class IOStream {
    private InputStream inStream;
    private OutputStream outStream;
    public static void processPersonsWithFunction(
            List<String> roster,
            Predicate<String> tester,
            Function<String, String> mapper,
            Consumer<String> block) {
        for (String p : roster) {
            if (tester.test(p)) {
                String data = mapper.apply(p);
                block.accept(data);
            }
        }
    }


    void IOStream()
    {
        TwoArgInterface plusOperation = (a, b) -> a + b;
        System.out.println("Sum of 10,34 : " + plusOperation.operation(10, 34));
        this.IOStream(null);
    }
    void IOStream(byte[] buffer)
    {

    }

    public void read()
    {

    }
}
