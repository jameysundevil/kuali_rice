/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.data.jpa;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ClassUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

class JpaQueryTranslator extends QueryTranslatorBase<Criteria, Query> {
    protected EntityManager entityManager;

    JpaQueryTranslator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Query createQuery(Class queryClazz, Criteria criteria) {
        return new QueryByCriteria(entityManager, criteria).toQuery();
    }

    @Override
    protected Criteria createCriteria(Class entityClass) {
        return new Criteria(entityClass.getName());
    }

    @Override
    protected Criteria createInnerCriteria(Criteria parent) {
        try {
            return createCriteria(ClassUtils.getDefaultClassLoader().loadClass(parent.getEntityName()));
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    @Override
    public void convertQueryFlags(org.kuali.rice.core.api.criteria.QueryByCriteria qbc, Query query) {
        //ojb's is 1 based, our query api is zero based
        //final int startAtIndex = criteria.getStartAtIndex() != null ? criteria.getStartAtIndex() + 1 : 1;
        // XXX: FIXME: had to change this to 0...why was this setting start index to 1?
        // experimentation shows that initial offset of 1 is required for OJB criteria tests to pass
        final int startAtIndex = qbc.getStartAtIndex() != null ? qbc.getStartAtIndex() : 0;
        query.setFirstResult(startAtIndex);

        if (qbc.getMaxResults() != null) {
            //not subtracting one from MaxResults in order to retrieve
            //one extra row so that the MoreResultsAvailable field can be set
            query.setMaxResults(qbc.getMaxResults());
        }
    }

    @Override
    protected void addNotNull(Criteria criteria, String propertyPath) {
        criteria.notNull(propertyPath);
    }

    @Override
    protected void addIsNull(Criteria criteria, String propertyPath) {
        criteria.isNull(propertyPath);
    }

    @Override
    protected void addEqualTo(Criteria criteria, String propertyPath, Object value) {
        criteria.eq(propertyPath, value);
    }

    @Override
    protected void addGreaterOrEqualTo(Criteria criteria, String propertyPath, Object value) {
        criteria.gte(propertyPath, value);
    }

    @Override
    protected void addGreaterThan(Criteria criteria, String propertyPath, Object value) {
        criteria.gt(propertyPath, value);
    }

    @Override
    protected void addLessOrEqualTo(Criteria criteria, String propertyPath, Object value) {
        criteria.lte(propertyPath, value);
    }

    @Override
    protected void addLessThan(Criteria criteria, String propertyPath, Object value) {
        criteria.lt(propertyPath, value);
    }

    @Override
    protected void addLike(Criteria criteria, String propertyPath, Object value) {
        criteria.like(propertyPath, value);
    }

    @Override
    protected void addNotEqualTo(Criteria criteria, String propertyPath, Object value) {
        criteria.ne(propertyPath, value);
    }

    @Override
    protected void addNotLike(Criteria criteria, String propertyPath, Object value) {
        criteria.notLike(propertyPath, value);
    }

    @Override
    protected void addIn(Criteria criteria, String propertyPath, Collection values) {
        criteria.in(propertyPath, values);
    }

    @Override
    protected void addNotIn(Criteria criteria, String propertyPath, Collection values) {
        criteria.notIn(propertyPath, values);
    }

    @Override
    protected void addAnd(Criteria criteria, Criteria inner) {
        criteria.and(inner);
    }

    @Override
    protected void addOr(Criteria criteria, Criteria inner) {
        criteria.or(inner);
    }

    @Override
    protected String genUpperFunc(String pp) {
        if (StringUtils.contains(pp, "__JPA_ALIAS[[")) {
            pp = "UPPER(" + pp + ")";
        } else {
            pp = "UPPER(__JPA_ALIAS[[0]]__." + pp + ")";
        }
        return pp;
    }
}
