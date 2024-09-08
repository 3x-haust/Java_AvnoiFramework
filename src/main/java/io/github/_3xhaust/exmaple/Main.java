package io.github._3xhaust.exmaple;

import io.github._3xhaust.Avnoi;
import io.github._3xhaust.AvnoiApplication;

@AvnoiApplication
public class Main {
    public static void main(String[] args) {
        Avnoi.run(Main.class, AppModule.class);
    }
}