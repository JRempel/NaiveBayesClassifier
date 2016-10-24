import java.math.BigDecimal;

public class ClassProperties {
    private BigDecimal probability;
    private BigDecimal totalFeatures;

    public ClassProperties() {
        probability = BigDecimal.ZERO;
    }

    public ClassProperties(BigDecimal count) {
        probability = BigDecimal.ZERO;
        totalFeatures = count;
    }

    public BigDecimal getProbability() {
        return this.probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = probability;
    }

    public BigDecimal getTotal() {
        return totalFeatures;
    }

    public void addTotal(BigDecimal amount) {
        this.totalFeatures = totalFeatures.add(amount);
    }
}
