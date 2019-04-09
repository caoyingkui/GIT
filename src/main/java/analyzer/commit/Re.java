package analyzer.commit;

import analyzer.histories.variation.MethodMutantType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Re {
    String issueType;
    Map<MethodMutantType, Integer> methods = new HashMap<>();
    public Re() {
        for (MethodMutantType type: MethodMutantType.values()) {
            methods.put(type, 0);
        }
    }

    public void add(MethodMutantType type) {
        if (methods.containsKey(type))
            methods.put(type, methods.get(type) + 1);
        else {
            int a = 2;
        }

    }

    public String toString() {
        String result = "";
        double total = 1;
        for (MethodMutantType type: methods.keySet()) {
            total +=  methods.get(type) ;
        }
        for (MethodMutantType type: MethodMutantType.values()) {
            result += "\t" + type.value + "\t" + String.format("%.5f",methods.get(type) / total)+ "\n";
        }
        return result;
    }

    public String toRow(int label) {
        StringBuilder row = new StringBuilder();
        double total = 0.00000000000001;
        for (MethodMutantType type: MethodMutantType.values()) {
            total += methods.get(type);
        }
        for (int i = 0; i <= 2; i += 1) {
            for (int j = 0; j <= 3; j += 1) {
                for (int k = 0; k <= 3; k += 1) {
                    if (i + j + k == 0) continue;
                    int count = 0;
                    for (MethodMutantType type: MethodMutantType.values()) {
                        int typeValue = type.value;
                        if ((i == 0 || typeValue / 100 == i) &&
                            (j == 0 || typeValue % 100 / 10 == j) &&
                            (k == 0 || typeValue % 100 % 10 == k)) {
                            count += methods.get(type);
                        }
                    }
                    row.append(String.format("%.5f", count / total)).append(" ");
                }
            }
        }
        row.append(label);
        return row.toString();
    }
}