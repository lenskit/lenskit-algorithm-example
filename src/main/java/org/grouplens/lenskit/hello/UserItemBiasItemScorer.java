/*
 * Copyright 2014 LensKit Contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grouplens.lenskit.hello;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The personalized mean item scorer.
 */
public class UserItemBiasItemScorer extends AbstractItemScorer {
    private final BiasModel model;
    private final UserEventDAO userEventDAO;

    /**
     * Construct the biased item scorer.  LensKit will automatically use this constructor to create
     * the item scorer.  The parameters express the <emph>dependencies</emph> of this component.
     * LensKit will make sure they are available and ready to provide.
     *
     * @param model The model, containing global and item average data.
     * @param userEventDAO A source of user rating data.
     */
    @Inject
    public UserItemBiasItemScorer(BiasModel model, UserEventDAO userEventDAO) {
        // save the model and DAO away so we can use them later
        this.model = model;
        this.userEventDAO = userEventDAO;
    }

    /**
     * Compute the scores.
     * @param user The user to personalize for.
     * @param scores The score vector.  This is both an input and an output parameter; its
     *               key domain contains the set of all items to score, and the method puts the
     *               scores into this vector.
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        // start with the global average for everything
        scores.fill(model.getGlobalMean());
        // add the item biases - missing items will be ignored
        scores.add(model.getItemBiases());

        // now we must compute the user bias.  get their ratings
        UserHistory<Rating> ratings = userEventDAO.getEventsForUser(user, Rating.class);
        if (ratings != null) {
            // compute the user's bias (average offset from item mean)
            // first get a vector of their ratings
            MutableSparseVector userVector = Ratings.userRatingVector(ratings);
            // then subtract the global mean
            userVector.add(-model.getGlobalMean());
            // and the item means
            userVector.addScaled(model.getItemBiases(), -1);
            // the compute the mean of the residuals...
            double userBias = userVector.mean();
            // ... and add it to the scores
            scores.add(userBias);
        } // otherwise: the user has no data, do not add anything
    }
}
