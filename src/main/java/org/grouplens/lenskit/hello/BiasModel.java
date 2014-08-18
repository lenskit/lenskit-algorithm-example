package org.grouplens.lenskit.hello;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Serializable;

/**
 * A 'model' object storing the precomputed item biases.  The {@link DefaultProvider} annotation
 * specifies how this model will be built: it will be built using the model builder class.  The
 * {@link Shareable} annotation tells LensKit that the model can be reused between different
 * recommenders.
 */
@DefaultProvider(BiasModelBuilder.class)
@Shareable
public class BiasModel implements Serializable {
    private static final long serialVersionUID = 1L;
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
