import java.math.BigDecimal;

public class FeatureProperties {
    private BigDecimal total;
    private BigDecimal probability;

    public FeatureProperties(BigDecimal total) {
        this.total = total;
        probability = BigDecimal.ZERO;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getProbability() {
        return this.probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = probability;
    }

    public void addTotal(BigDecimal amount) {
        this.total = this.total.add(amount);
    }
}
