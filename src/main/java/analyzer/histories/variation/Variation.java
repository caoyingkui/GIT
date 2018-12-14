package analyzer.histories.variation;

import analyzer.histories.EventDate;

import java.util.ArrayList;
import java.util.List;

public class Variation {
    public String commitId;
    public String methodName;
    public EventDate date;
    public List<Mutant> mutants = new ArrayList<>();

    public void addMutant(Mutant mutant) {
        mutants.add(mutant);
    }

    public void addMutant(List<Mutant> mutants) {
        this.mutants.addAll(mutants);
    }
}
