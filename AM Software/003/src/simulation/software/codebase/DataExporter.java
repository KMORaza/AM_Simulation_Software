package simulation.software.codebase;

import java.io.FileWriter;
import java.io.IOException;

public class DataExporter {
    public static void exportToCSV(AMSignal signal, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Time,Message,Carrier,Modulated,Demodulated\n");
            double[] time = signal.getTime();
            double[] message = signal.getMessage();
            double[] carrier = signal.getCarrier();
            double[] modulated = signal.getSignal();
            double[] demodulated = signal.getDemodulatedSignal();
            for (int i = 0; i < time.length; i++) {
                writer.write(String.format("%.6f,%.6f,%.6f,%.6f,%.6f\n",
                        time[i], message[i], carrier[i], modulated[i], demodulated[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}