package org.miejski.simple.objects.consistecy;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EventsPercentages {

    private final List<Integer> groups;
    private final Random rand;
    private final List<Double> thresholds;

    public EventsPercentages(int createPercentage, int updatePercentage, int deletePercentage) {
        if (createPercentage + updatePercentage + deletePercentage != 100) {
            throw new RuntimeException("Invalid percentage sum");
        }
        this.groups = Arrays.asList(1, 2, 3);
        this.rand = new Random();

        this.thresholds = Arrays.asList(createPercentage / 100.0, (createPercentage + updatePercentage) / 100.0, (100.0 - deletePercentage) / 100.0);
    }

    public List<Integer> getGroups() {
        return this.groups;
    }

    public Integer next() {
        float f = rand.nextFloat();
        if (f < this.thresholds.get(0)) {
            return this.groups.get(0);
        }
        if (f < this.thresholds.get(1)) {
            return this.groups.get(1);
        } else {
            return this.groups.get(2);
        }
    }
}
