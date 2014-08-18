package org.grouplens.lenskit.hello;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * A 'model' object storing the precomputed item biases.
 */
@DefaultProvider(BiasModelBuilder.class)
public class BiasModel {
    private final double globalMean;
    private final ImmutableSparseVector itemBiases;

    public BiasModel(double mean, SparseVector biases) {
        globalMean = mean;
        itemBiases = biases.immutable();
    }

    /**
     * Get the global mean rating.
     * @return The global mean rating.
     */
    public double getGlobalMean() {
        return globalMean;
    }

    /**
     * Get the per-item biases (the difference between the item's average rating and the
     * global average).
     * @return A vector of item biases.
     */
    public SparseVector getItemBiases() {
        return itemBiases;
    }
}
