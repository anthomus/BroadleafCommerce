/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.rating.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.profile.util.EntityConfiguration;
import org.broadleafcommerce.rating.domain.RatingSummary;
import org.broadleafcommerce.rating.service.type.RatingType;
import org.springframework.stereotype.Repository;

@Repository("blRatingSummaryDao")
public class RatingSummaryDaoImpl implements RatingSummaryDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource
    protected EntityConfiguration entityConfiguration;

    protected String queryCacheableKey = "org.hibernate.cacheable";

    @SuppressWarnings("unchecked")
	public void deleteRatingSummary(RatingSummary summary) {
    	if (!em.contains(summary)) {
    		summary = (RatingSummary) em.find(entityConfiguration.lookupEntityClass(RatingSummary.class.getName()), summary.getId());
    	}
        em.remove(summary);
    }

    public RatingSummary saveRatingSummary(RatingSummary summary) {
        summary.resetAverageRating();
        return em.merge(summary);
    }

    @SuppressWarnings("unchecked")
    public List<RatingSummary> readRatingSummaries(List<String> itemIds, RatingType type) {
        Query query = em.createNamedQuery("BC_READ_RATING_SUMMARIES_BY_ITEM_ID_AND_TYPE");
        query.setParameter("itemIds", itemIds);
        query.setParameter("ratingType", type);
        List<RatingSummary> ratings = query.getResultList();

        return ratings;
    }

    public RatingSummary readRatingSummary(String itemId, RatingType type) {
        Query query = em.createNamedQuery("BC_READ_RATING_SUMMARIES_BY_ITEM_ID_AND_TYPE");
        query.setParameter("itemId", itemId);
        query.setParameter("ratingType", type);
        RatingSummary ratingSummary = (RatingSummary) query.getSingleResult();

        return ratingSummary;
    }

}
