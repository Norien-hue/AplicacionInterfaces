package com.javafx.deprecated;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SimpleChartSample extends Application {
    private static final int N_SAMPLES = 100;

    @Override
    public void start(final Stage primaryStage) {
        final StackPane root = new StackPane();

        // 1. Crear el gráfico con sus ejes X e Y
        final XYChart chart = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        root.getChildren().add(chart);

        // 2. Crear conjuntos de datos
        final DoubleDataSet dataSet1 = new DoubleDataSet("data set #1");
        final DoubleDataSet dataSet2 = new DoubleDataSet("data set #2");
        chart.getDatasets().addAll(dataSet1, dataSet2); // Añadirlos al gráfico

        // 3. Generar datos de ejemplo (una onda coseno y una seno)
        final double[] xValues = new double[N_SAMPLES];
        final double[] yValues1 = new double[N_SAMPLES];
        final double[] yValues2 = new double[N_SAMPLES];
        for (int n = 0; n < N_SAMPLES; n++) {
            xValues[n] = n;
            yValues1[n] = Math.cos(Math.toRadians(10.0 * n));
            yValues2[n] = Math.sin(Math.toRadians(10.0 * n));
        }

        // 4. Asignar los datos a los DataSet
        dataSet1.set(xValues, yValues1);
        dataSet2.set(xValues, yValues2);

        // 5. Configurar y mostrar la ventana
        final Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("ChartFx Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        Application.launch(args);
    }
}