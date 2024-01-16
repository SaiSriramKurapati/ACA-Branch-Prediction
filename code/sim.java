import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class sim {
    private static int no_of_mispreds = 0;
    private static double no_of_input_bits;
    private static double value_of_n_bit;
    private static double gsh_hy_input_bits;
    private static double no_of_pc_bits;
    private static double chose_bit;
    private static final int NUM_TAGE_TABLES = 100;
    private static final int COUNTER_MAX = 3;
    private static List<String> tnt_bits = new ArrayList<>();
    private static List<String> total_preds = new ArrayList<>();

    // SMITH N-BIT BRANCH PREDICTOR
    public static int smith_predictor(String predictor, String inputBit, String traceFile) {

        double pow_bit = 0;
        no_of_mispreds = 0;
        value_of_n_bit = Math.pow(2, no_of_input_bits);
        pow_bit = Math.pow(2, no_of_input_bits - 1);
        for (int i = 0; i < total_preds.size(); i++) {
            if (tnt_bits.get(i).equals("t")) {
                if (pow_bit < Math.pow(2, (int) no_of_input_bits - 1)) {
                    no_of_mispreds += 1;
                }
                pow_bit += 1;                 //if the branch = taken then the value is incremented
                if (pow_bit > value_of_n_bit - 1) {
                    pow_bit = value_of_n_bit - 1;
                }
            } else if (tnt_bits.get(i).equals("n")) {
                if (pow_bit >= Math.pow(2, (int) no_of_input_bits - 1)) {
                    no_of_mispreds += 1;
                }
                pow_bit -= 1; //if the branch = not-taken then the value is decremented
                if (pow_bit < 0) {
                    pow_bit = 0;
                }
            }
        }
        System.out.println("COMMAND");
        System.out.println("./sim " + predictor + " " + inputBit + " " + traceFile);
        output();
        System.out.print("FINAL COUNTER CONTENT:        ");
        System.out.println((int) pow_bit);
        return 0;
    }

    // BIMODAL BRANCH PREDICTOR
    public static int bimodal_predictor(String predictor, String inputBits, String traceFile) {
        no_of_mispreds = 0;
        value_of_n_bit = Math.pow(2, (int) no_of_input_bits);
        double[] pred = new double[(int) value_of_n_bit];
        for (int i = 0; i < (int) value_of_n_bit; i++) {
            pred[i] = 4;
        }
        for (int i = 0; i < total_preds.size(); i++) {
            String pc_branch = Integer.toBinaryString(Integer.parseInt(total_preds.get(i), 16));
            int length = pc_branch.length();
            int idx = Integer.parseInt(pc_branch.substring(length - ((int) no_of_input_bits) - 2, length - 2), 2); // bimodal index calculation
            if (tnt_bits.get(i).equals("t")) {
                if (pred[idx] < 4) {
                    no_of_mispreds += 1;
                }
                pred[idx] += 1;             //if the branch = taken then the prediction value is incremented at the index
                if (pred[idx] > 7) {
                    pred[idx] = 7;
                }
            }
            if (tnt_bits.get(i).equals("n")) {
                if (pred[idx] >= 4) {
                    no_of_mispreds += 1;
                }
                pred[idx] -= 1;           //if the branch = not taken then the prediction value is decremented at the index
                if (pred[idx] < 0) {
                    pred[idx] = 0;
                }
            }
        }
        System.out.println("COMMAND");
        System.out.println("./sim " + predictor + " " + inputBits + " " + traceFile);
        output();
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0; i < pred.length; i++) {
            System.out.print(i + "	");
            System.out.println((int) pred[i]);

        }
        return 0;
    }

    //GSHARE BRANCH PREDICTOR
    public static int gshare_predictor(String predictor, String pcBits, String gshareHybInputBits, String traceFile) {
        no_of_mispreds = 0;
        value_of_n_bit = Math.pow(2, (int) no_of_pc_bits);
        double[] pred = new double[(int) value_of_n_bit];
        List<String> global_branch_hist_reg = new ArrayList<>();
        for (int i = 0; i < (int) gsh_hy_input_bits; i++)                 // global branch history register initialization
        {
            global_branch_hist_reg.add("0");
        }
        for (int i = 0; i < value_of_n_bit; i++) {
            pred[i] = 4;
        }
        for (int i = 0; i < total_preds.size(); i++) {
            String gb_hist_reg = "";
            for (int j = 0; j < (int) gsh_hy_input_bits; j++) {
                gb_hist_reg += global_branch_hist_reg.get(j);
            }
            String pc_branch = Integer.toBinaryString(Integer.parseInt(total_preds.get(i), 16));
            int length = pc_branch.length();
            String pc_v = pc_branch.substring(length - ((int) no_of_pc_bits) - 2, length - 2).substring((int) no_of_pc_bits - (int) gsh_hy_input_bits);
            String pc_address = pc_branch.substring(length - ((int) no_of_pc_bits) - 2, length - 2).substring(0, ((int) no_of_pc_bits - (int) gsh_hy_input_bits));
            String xor_bit = "";
            for (int j = 0; j < (int) gsh_hy_input_bits; j++)   // XORing global share branch history with pc last n bits
            {
                if (pc_v.charAt(j) == gb_hist_reg.charAt(j)) {
                    xor_bit += "0";
                } else {
                    xor_bit += "1";
                }
            }
            int idx = Integer.parseInt(pc_address + xor_bit, 2);   // gshare index calculation

            if (tnt_bits.get(i).equals("t")) {
                if (pred[idx] < 4) {
                    no_of_mispreds += 1;
                }
                pred[idx] += 1;
                if (pred[idx] > 7) {
                    pred[idx] = 7;
                }
                global_branch_hist_reg.add(0, "1");         //here right bitwise shift is performed which will update the global branch history register and also the most significant bit is updated to 1
                global_branch_hist_reg.remove((int) gsh_hy_input_bits);
            }
            if (tnt_bits.get(i).equals("n")) {
                if (pred[idx] >= 4) {
                    no_of_mispreds += 1;
                }
                pred[idx] -= 1;
                if (pred[idx] < 0) {
                    pred[idx] = 0;
                }
                global_branch_hist_reg.add(0, "0");      //here right bitwise shift is performed which will update the global branch history register and also the most significant bit is updated to 0
                global_branch_hist_reg.remove((int) gsh_hy_input_bits);
            }
        }
        System.out.println("COMMAND");
        System.out.println("./sim " + predictor + " " + pcBits + " " + gshareHybInputBits + " " + traceFile);
        output();
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0; i < pred.length; i++) {
            System.out.print(i + "	");
            System.out.println((int) pred[i]);
        }
        return 0;
    }


    // HYBRID BRANCH PREDICTOR
    public static int hybrid_predictor(String predictor, String chooseBit, String pcBit, String gshHyInputBit, String inputBits, String traceFile) {
        no_of_mispreds = 0;
        value_of_n_bit = Math.pow(2, no_of_input_bits);
        int[] pred = new int[(int) value_of_n_bit];
        for (int i = 0; i < (int) value_of_n_bit; i++) {
            pred[i] = 4;
        }
        int idx_b;
        int[] g_pred = new int[(int) Math.pow(2, no_of_pc_bits)];
        for (int i = 0; i < (int) Math.pow(2, no_of_pc_bits); i++)              //the prediction table of gshare is initialized
        {
            g_pred[i] = 4;
        }
        List<String> global_branch_hist_reg = new ArrayList<>();
        for (int i = 0; i < (int) gsh_hy_input_bits; i++) {
            global_branch_hist_reg.add("0");
        }
        int idx_g;
        int[] chose_table = new int[(int) Math.pow(2, chose_bit)];
        for (int i = 0; i < (int) Math.pow(2, chose_bit); i++) {
            chose_table[i] = 1;                                       // chooser table initialization
        }
        for (int i = 0; i < total_preds.size(); i++) {
            String gb_hist_reg = "";
            for (int j = 0; j < (int) gsh_hy_input_bits; j++) {
                gb_hist_reg += global_branch_hist_reg.get(j);
            }
            String pc_branch = Integer.toBinaryString(Integer.parseInt(total_preds.get(i), 16));
            int length = pc_branch.length();
            int idx_hy = Integer.parseInt(pc_branch.substring(length - ((int) chose_bit) - 2, length - 2), 2); //hybrid index is calculated

            String pc_v = pc_branch.substring(length - ((int) no_of_pc_bits) - 2, length - 2).substring((int) no_of_pc_bits - (int) gsh_hy_input_bits);
            String pc_address = pc_branch.substring(length - ((int) no_of_pc_bits) - 2, length - 2).substring(0, ((int) no_of_pc_bits - (int) gsh_hy_input_bits));
            String xor_bit = "";
            for (int j = 0; j < (int) gsh_hy_input_bits; j++) {
                if (pc_v.charAt(j) == gb_hist_reg.charAt(j)) {
                    xor_bit += "0";
                } else {
                    xor_bit += "1";
                }
            }
            idx_g = Integer.parseInt(pc_address + xor_bit, 2);  //setting the index of gshare to hybrid
            idx_b = Integer.parseInt(pc_branch.substring(length - ((int) no_of_input_bits) - 2, length - 2), 2); //setting the index of bimodal to hybrid
            int g_bit = g_pred[idx_g];
            int b_bit = pred[idx_b];
            if (tnt_bits.get(i).equals("t")) {
                if (chose_table[idx_hy] >= 2)             // check to choose either bimodal or gshare prediction
                {
                    if (g_pred[idx_g] < 4)                 //if we consider gshare
                    {
                        no_of_mispreds += 1;
                    }

                    g_pred[idx_g] += 1;
                    if (g_pred[idx_g] > 7)                  // the prediction table of gshare is updated
                    {
                        g_pred[idx_g] = 7;
                    }
                } else if (chose_table[idx_hy] < 2)       //else if we consider bimodal
                {
                    if (pred[idx_b] < 4) {
                        no_of_mispreds += 1;
                    }
                    pred[idx_b] += 1;
                    if (pred[idx_b] > 7) {              // the prediction table of bimodal is updated
                        pred[idx_b] = 7;
                    }
                }
                global_branch_hist_reg.add(0, "1");    // without considering the chooser table the global branch history register is updated
                global_branch_hist_reg.remove((int) gsh_hy_input_bits);

                if (b_bit >= 4 && g_bit < 4) {
                    chose_table[idx_hy] -= 1;           // case 1 : bimodal = correct && gshare = incorrect then the chooser table is updated by decreasing the index value
                    if (chose_table[idx_hy] < 0) {
                        chose_table[idx_hy] = 0;
                    }
                } else if (b_bit < 4 && g_bit >= 4) {
                    chose_table[idx_hy] += 1;           //case 2 : bimodal = incorrect && gshare = correct then the chooser table is updated by increasing the index value
                    if (chose_table[idx_hy] > 3) {
                        chose_table[idx_hy] = 3;
                    }
                }

            } else if (tnt_bits.get(i).equals("n")) {
                if (chose_table[idx_hy] >= 2) {
                    if (g_pred[idx_g] >= 4) {
                        no_of_mispreds += 1;
                    }
                    g_pred[idx_g] -= 1;
                    if (g_pred[idx_g] < 0) {
                        g_pred[idx_g] = 0;
                    }
                } else if (chose_table[idx_hy] < 2) {
                    if (pred[idx_b] >= 4) {
                        no_of_mispreds += 1;
                    }
                    pred[idx_b] -= 1;
                    if (pred[idx_b] < 0) {
                        pred[idx_b] = 0;
                    }
                }
                global_branch_hist_reg.add(0, "0");
                global_branch_hist_reg.remove((int) gsh_hy_input_bits);

                if (b_bit >= 4 && g_bit < 4) {
                    chose_table[idx_hy] += 1;
                    if (chose_table[idx_hy] > 3) {
                        chose_table[idx_hy] = 3;
                    }
                } else if (b_bit < 4 && g_bit >= 4) {
                    chose_table[idx_hy] -= 1;
                    if (chose_table[idx_hy] < 0) {
                        chose_table[idx_hy] = 0;
                    }
                }
            }


        }
        System.out.println("COMMAND");
        System.out.println("./sim " + predictor + " " + chooseBit + " " + pcBit + " " + gshHyInputBit + " " + inputBits + " " + traceFile);
        output();
        System.out.println("FINAL CHOOSER CONTENTS");
        for (int i = 0; i < chose_table.length; i++) {
            System.out.print(i + "	");
            System.out.println(chose_table[i]);
        }
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0; i < g_pred.length; i++) {
            System.out.print(i + "	");
            System.out.println(g_pred[i]);
        }
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0; i < pred.length; i++) {
            System.out.print(i + "	");
            System.out.println(pred[i]);
        }
        return 0;
    }

    private static void output() {
        System.out.println("OUTPUT");
        System.out.print("number of predictions:        ");
        System.out.println(total_preds.size());
        System.out.print("number of mispredictions:     ");
        System.out.println(no_of_mispreds);
        System.out.print("misprediction rate:           ");
        System.out.println(String.format("%.2f", new BigDecimal(Float.toString((float) no_of_mispreds / total_preds.size() * 100))) + "%");
    }

    // MAIN FUNCTION
    public static void main(String args[])
    {
        switch(args[0])
        {
            // according to the first argument the branch predictors are switched
            case "smith":
                try{
                    FileInputStream file = new FileInputStream(args[2]);    // trace file is read
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine())
                    {
                        String[] data = sc.nextLine().toString().split(" "); // the data in the trace file is split
                        tnt_bits.add(data[1]);
                        total_preds.add(data[0]);                            // to branch array the branch address is added
                    }
                    sc.close();
                }
                catch(Exception exception)
                {
                    System.out.println(exception);
                }
                no_of_input_bits=Double.parseDouble(args[1]);
                smith_predictor(args[0],args[1],args[2]);
                break;
            case "bimodal":
                try{
                    FileInputStream file = new FileInputStream(args[2]);
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine())
                    {
                        String[] data = sc.nextLine().toString().split(" ");
                        tnt_bits.add(data[1]);
                        total_preds.add(data[0]);
                    }
                    sc.close();
                }
                catch(Exception exception)
                {
                    System.out.println(exception);
                }
                no_of_input_bits=Double.parseDouble(args[1]);
                bimodal_predictor(args[0],args[1],args[2]);
                break;
            case "gshare":
                try{
                    FileInputStream file = new FileInputStream(args[3]);
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine())
                    {
                        String[] data = sc.nextLine().toString().split(" ");
                        tnt_bits.add(data[1]);
                        total_preds.add(data[0]);
                    }
                    sc.close();
                }
                catch(Exception exception)
                {
                    System.out.println(exception);
                }
                no_of_pc_bits=Double.parseDouble(args[1]);
                gsh_hy_input_bits=Double.parseDouble(args[2]);
                gshare_predictor(args[0],args[1],args[2],args[3]);
                break;
            case "hybrid":
                try{
                    FileInputStream file = new FileInputStream(args[5]);
                    Scanner sc = new Scanner(file);
                    while(sc.hasNextLine())
                    {
                        String[] data = sc.nextLine().toString().split(" ");
                        tnt_bits.add(data[1]);
                        total_preds.add(data[0]);
                    }
                    sc.close();
                }
                catch(Exception exception)
                {
                    System.out.println(exception);
                }
                chose_bit=Double.parseDouble(args[1]);
                no_of_pc_bits=Double.parseDouble(args[2]);
                gsh_hy_input_bits=Double.parseDouble(args[3]);
                no_of_input_bits=Double.parseDouble(args[4]);
                hybrid_predictor(args[0],args[1],args[2],args[3],args[4],args[5]);
            case "tage":
                try {
                    FileInputStream file = new FileInputStream(args[2]);
                    int tageSize = Integer.valueOf(args[1]);
                    Scanner sc = new Scanner(file);
                    int mispredictions = 0;
                    int totalPredictions = 0;
                    String line;

                    // Initialize TAGE tables.
                    TageTable[] tageTables = new TageTable[NUM_TAGE_TABLES];
                    for (int i = 0; i < NUM_TAGE_TABLES; i++) {
                        tageTables[i] = new TageTable(tageSize, COUNTER_MAX);
                    }
                    while(sc.hasNextLine())
                    {
                        // Parse address and outcome from trace file.
                        String[] parts = sc.nextLine().toString().split(" ");
                        long address = Long.parseLong(parts[0], 16);
                        boolean outcome = parts[1].equals("t");

                        // Run TAGE predictor on the current address.
                        boolean prediction = predict(tageTables, address,tageSize);

                        // Update mispredictions count.
                        if (prediction != outcome) {
                            mispredictions++;
                        }

                        // Update TAGE tables.
                        update(tageTables, address, outcome,Integer.valueOf(args[1]));

                        totalPredictions++;
                    }

                    sc.close();

                    System.out.println("Total predictions: " + totalPredictions);
                    System.out.println("Mispredictions: " + mispredictions);
                    System.out.println("Misprediction rate: " + (100.0 * mispredictions / totalPredictions) + "%");

                } catch (IOException e) {
                    System.err.println("Error reading trace file: " + e.getMessage());
                }

        }

    }
    private static boolean predict(TageTable[] tageTables, long address, int tageSize) {
        int[] indices = calculateIndices(address,tageSize);
        int[] tags = calculateTags(address);

        // Search for the longest matching entry in the TAGE tables.
        TageEntry longestMatch = null;
        for (int i = NUM_TAGE_TABLES - 1; i >= 0; i--) {
            TageEntry entry = tageTables[i].getEntry(indices[i]);
            if (entry.getTag() == tags[i]) {
                longestMatch = entry;
                break;
            }
        }

        if (longestMatch != null) {
            return longestMatch.getCounter().getValue() > (COUNTER_MAX / 2);
        } else {
            return false; // Default prediction when no matching entry is found.
        }
    }

    private static void update(TageTable[] tageTables, long address, boolean outcome,int tageSize) {
        int[] indices = calculateIndices(address,tageSize);
        int[] tags = calculateTags(address);

        // Update matching entries and allocate new entries if necessary.
        for (int i = 0; i < NUM_TAGE_TABLES; i++) {
            TageEntry entry = tageTables[i].getEntry(indices[i]);

            if (entry.getTag() == tags[i]) {
                // Update the counter of the matching entry.
                if (outcome) {
                    entry.getCounter().increment();
                } else {
                    entry.getCounter().decrement();
                }

                // Allocate new entries with a certain probability.
                // This is a simplified version and needs refinement for better accuracy.
                if (i > 0 && Math.random() < (1.0 / (1 << i))) {
                    entry.setTag(tags[i]);
                }
            }
        }
    }

    // Implement the functions to calculate indices and tags for TAGE tables.
    // These are simplified versions and need refinement for better accuracy.

    private static int[] calculateIndices(long address, int tageSize) {
        int[] indices = new int[NUM_TAGE_TABLES];
        for (int i = 0; i < NUM_TAGE_TABLES; i++) {
            indices[i] = (int) (address ^ (address >>> (i + 1))) & (tageSize - 1);
        }
        return indices;
    }

    private static int[] calculateTags(long address) {
        int[] tags = new int[NUM_TAGE_TABLES];
        for (int i = 0; i < NUM_TAGE_TABLES; i++) {
            tags[i] = (int) (address >>> (i + 1));
        }
        // System.out.println(tags.length);
        return tags;
    }
}