package io.github._3xhaust.exmaple.initial;

import io.github._3xhaust.Avnoi;
import io.github._3xhaust.annotations.AvnoiApplication;

@AvnoiApplication
public class Main {
    public static void main(String[] args) {
        Avnoi.listen(3000);
        Avnoi.run(AppModule.class);
    }
}