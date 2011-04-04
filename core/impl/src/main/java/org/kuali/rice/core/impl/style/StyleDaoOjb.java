/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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
package org.kuali.rice.core.impl.style;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;


public class StyleDaoOjb extends PersistenceBrokerDaoSupport implements StyleDao {

	@Override
    public void saveStyle(StyleBo styleData) {
        this.getPersistenceBrokerTemplate().store(styleData);
    }

	@Override
    public StyleBo getStyle(String styleName) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("name", styleName);
        criteria.addEqualTo("active", Boolean.TRUE);
        return (StyleBo) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(StyleBo.class, criteria));
    }

}
