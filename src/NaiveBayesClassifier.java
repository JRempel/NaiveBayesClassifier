import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO: Address Zero-Frequency Issue
//       Laplace estimator?
// TODO: K-Fold Cross-Validation

public class NaiveBayesClassifier<T, K> {
    /**
     * FeatureProperties.total = total amount of this type of feature.
     * ClassProperties.total = total amount of ANY type of feature in this class.
     * relation BigDecimal = total number of this type of feature in this class.
     * totalFeatures = total amount of ANY type of feature in any class.
     * totalClasses = the number of classes in the training data.
     */
    private HashMap<T, ClassProperties> classifications = new HashMap<>();
    private HashMap<K, FeatureProperties> features = new HashMap<>();
    private HashMap<T, HashMap<K, BigDecimal>> relationCount;
    private HashMap<T, HashMap<K, BigDecimal>> featureGivenClass;
    private BigDecimal totalFeatures = BigDecimal.ZERO;
    private BigDecimal totalClasses = BigDecimal.ZERO;
    private boolean trained;

    public NaiveBayesClassifier() {
        trained = false;
        totalClasses = BigDecimal.ZERO;
        totalFeatures = BigDecimal.ZERO;
    }

    /**
     * Returns the set of all classifications added
     * as training data to the classifier.
     * @return
     */
    public Set<T> getClassifications() {
        return classifications.keySet();
    }

    /**
     * Returns the set of all features added
     * as training data to the classifier.
     * @return
     */
    public Set<K> getFeatures() {
        return features.keySet();
    }

    // TODO: check later if Properties contain something additional that needs to be added

    /**
     * Add a mapping of one class to many features, with the amount of each
     * feature in said class.
     * @param classification
     * @param features
     */
    public void addTrainingData(T classification, Map<K, BigDecimal> features) {
        for (Map.Entry<K, BigDecimal> entry : features.entrySet()) {
            addTrainingData(classification, entry.getKey(), entry.getValue());
        }
        trained = false;
    }

    /**
     * Add a mapping of one class to one feature, with the amount of said
     * feature in said class.
     * @param classification
     * @param feature
     * @param count
     */
    public void addTrainingData(T classification, K feature, BigDecimal count) {
        ClassProperties classProperties = classifications.get(classification);
        if (classProperties != null) {
            classProperties.addTotal(count);
        } else {
            classifications.put(classification, new ClassProperties(count));
            totalClasses = totalClasses.add(BigDecimal.ONE);
        }
        FeatureProperties featureProperties = features.get(feature);
        if (featureProperties != null) {
            featureProperties.addTotal(count);
        } else {
            features.put(feature, new FeatureProperties(count));
        }
        HashMap<K, BigDecimal> entryMap = relationCount.get(classification);
        if (entryMap != null) {
            if (entryMap.get(feature) != null) {
                BigDecimal newCount = entryMap.get(feature);
                newCount = newCount.add(count);
                entryMap.put(feature, newCount);
            } else {
                entryMap.put(feature, count);
            }
        } else {
            entryMap = new HashMap<>();
            entryMap.put(feature, count);
            relationCount.put(classification, entryMap);
        }
        totalFeatures = totalFeatures.add(count);
        trained = false;
    }

    /**
     * Calculates the necessary probabilities for classification, for
     * all items in the training set.
     * @throws Exception
     */
    public void train() throws Exception {
        if (totalClasses.compareTo(BigDecimal.valueOf(2)) < 0 || totalFeatures.compareTo(BigDecimal.ZERO) == 0 || relationCount.size() < 2) {
            throw new Exception("Training requires at least two classes, more than zero features, and at least 2 relations.");
        }

        // Probability for Class<T>
        ClassProperties classProperties;
        for (T classification : classifications.keySet()) {
            classProperties = classifications.get(classification);
            classProperties.setProbability(classProperties.getTotal().divide(totalFeatures, BigDecimal.ROUND_HALF_UP));
        }

        // Probability for each Feature<K>
        FeatureProperties featureProperties;
        for (K feature : features.keySet()) {
            featureProperties = features.get(feature);
            featureProperties.setProbability(featureProperties.getTotal().divide(totalFeatures, BigDecimal.ROUND_HALF_UP));
        }

        // Probability of a feature, given a specific class
        // # (features in class / total features) * (Probability of class) / (Probability of feature)
        featureGivenClass = new HashMap<>();
        HashMap<K, BigDecimal> featureProbability;
        BigDecimal probability;
        for (T classification : relationCount.keySet()) {
            HashMap<K, BigDecimal> tempMap = relationCount.get(classification);
            featureProbability = new HashMap<>();
            for (K feature : tempMap.keySet()) {
                probability = BigDecimal.ONE.multiply(tempMap.get(feature)
                        .divide(totalFeatures, BigDecimal.ROUND_HALF_UP)
                        .multiply(classifications.get(classification).getProbability())
                        .divide(features.get(feature).getProbability(), BigDecimal.ROUND_HALF_UP));
                featureProbability.put(feature, probability);
                featureGivenClass.put(classification, featureProbability);
            }
        }
        trained = true;
    }

    /**
     * Returns the posterior probability of the feature being in the
     * most likely class.
     * @param feature
     * @return
     * @throws Exception
     */
    public BigDecimal classify(K feature) throws Exception {
        if (trained) {
            ArrayList<BigDecimal> probabilities = new ArrayList<>();
            for (T classification : classifications.keySet()) {
                probabilities.add(classify(classification, feature));
            }
            Collections.sort(probabilities);
            return probabilities.get(probabilities.size() - 1);
        } else {
            throw new Exception("NaiveBayesClassifier must be trained first.");
        }
    }

    /**
     * Returns the posterior probability of the feature being
     * in the specified class.
     * @param classification
     * @param feature
     * @return
     */
    public BigDecimal classify(T classification, K feature) throws Exception {
        if (trained) {
            return featureGivenClass.get(classification).get(feature)
                    .multiply(classifications.get(classification).getProbability())
                    .divide(features.get(feature).getProbability(), BigDecimal.ROUND_HALF_UP);
        } else {
            throw new Exception("NaiveBayesClassifier must be trained first.");
        }
    }

    /**
     * Returns the posterior probability of all features being
     * in the most likely class.
     * @param features
     * @return
     */
    public BigDecimal classify(Set<K> features) throws Exception {
        if (trained) {
            ArrayList<BigDecimal> probabilities = new ArrayList<>();
            for (T classification : classifications.keySet()) {
                probabilities.add(classify(classification, features));
            }
            Collections.sort(probabilities);
            return probabilities.get(probabilities.size() - 1);
        } else {
            throw new Exception("NaiveBayesClassifier must be trained first.");
        }
    }

    /**
     * Returns the posterior probability of all features being
     * in the specified class.
     * @param classification
     * @param features
     * @return
     */
    public BigDecimal classify(T classification, Set<K> features) throws Exception {
        if (trained) {
            BigDecimal result = classifications.get(classification).getProbability();
            for (K feature : features) {
                result = result.multiply(featureGivenClass.get(classification).get(feature))
                        .divide(this.features.get(feature).getProbability(), BigDecimal.ROUND_HALF_UP);
            }
            return result;
        } else {
            throw new Exception("NaiveBayesClassifier must be trained first.");
        }
    }
}