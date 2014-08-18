package org.grouplens.lenskit.hello;

import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.util.IdMeanAccumulator;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Model builder that computes the global and item biases.
 */
public class BiasModelBuilder implements Provider<BiasModel> {
    private final EventDAO dao;

    /**
     * Construct the model builder.
     * @param dao The event DAO to get rating data.  That this is {@link Transient} means that the
     *            builder will use the data access object to build the model, but the model will
     *            not need the data access object once it is built.  This allows LensKit to cache
     *            and share the model.  If the model will keep a reference to the object, then it
     *            cannot be marked as transient.
     */
    @Inject
    public BiasModelBuilder(@Transient EventDAO dao) {
        this.dao = dao;
    }

    /**
     * Build the bias model.
     * @return An object containing the precomputed global mean and per-item offsets.
     */
    @Override
    public BiasModel get() {
        // we will use an ID mean accumulator to compute the means
        IdMeanAccumulator accum = new IdMeanAccumulator();

        Cursor<Rating> ratings = dao.streamEvents(Rating.class);
        try {
            /* We loop over all ratings.  The 'fast()' improves performance for the common case,
             * when we will only work with the rating object inside the loop body.
             *
             * If the data set may have multiple ratings for the same (user,item) pair, this code
             * will be not quite correct.
             */
            for (Rating r: ratings.fast()) {
                Preference pref = r.getPreference();
                if (pref != null) {
                    accum.put(pref.getItemId(), pref.getValue());
                }
            }
        } finally {
            // cursors must be closed
            ratings.close();
        }

        // Build the model, applying a little damping to item means
        return new BiasModel(accum.globalMean(), accum.idMeanOffsets(5));
    }
}
