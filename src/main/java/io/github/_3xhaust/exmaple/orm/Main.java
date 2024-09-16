package io.github._3xhaust.exmaple.orm;

import io.github._3xhaust.Avnoi;
import io.github._3xhaust.annotations.AvnoiApplication;

import java.util.List;
import java.util.Map;

@AvnoiApplication
public class Main {
    public static void main(String[] args) {
        Avnoi.enableCors();
        Avnoi.run(AppModule.class);
    }
}