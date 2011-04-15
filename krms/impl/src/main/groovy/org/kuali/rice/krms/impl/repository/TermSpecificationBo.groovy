package org.kuali.rice.krms.impl.repository

import org.kuali.rice.kns.bo.Inactivateable
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase
import org.kuali.rice.krms.api.repository.TermSpecificationDefinition
import org.kuali.rice.krms.api.repository.TermSpecificationDefinitionContract

public class TermSpecificationBo extends PersistableBusinessObjectBase implements TermSpecificationDefinitionContract{

	def String id
	def String contextId
	def String name
	def String type
		
	/**
	* Converts a mutable bo to it's immutable counterpart
	* @param bo the mutable business object
	* @return the immutable object
	*/
   static TermSpecificationDefinition to(TermSpecificationBo bo) {
	   if (bo == null) { return null }
	   return org.kuali.rice.krms.api.repository.TermSpecificationDefinition.Builder.create(bo).build()
   }

   /**
	* Converts a immutable object to it's mutable bo counterpart
	* @param im immutable object
	* @return the mutable bo
	*/
   static TermSpecificationBo from(TermSpecificationDefinition im) {
	   if (im == null) { return null }

	   TermSpecificationBo bo = new TermSpecificationBo()
	   bo.id = im.id
	   bo.contextId = im.contextId
	   bo.name = im.name
	   bo.type = im.type

	   return bo
   }
 
} 