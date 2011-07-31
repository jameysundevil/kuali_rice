/*
 * Copyright 2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.krad.datadictionary.AttributeDefinition;
import org.kuali.rice.krad.datadictionary.BusinessObjectEntry;
import org.kuali.rice.krad.datadictionary.DataDictionary;
import org.kuali.rice.krad.datadictionary.DataObjectEntry;
import org.kuali.rice.krad.datadictionary.validation.Address;
import org.kuali.rice.krad.datadictionary.validation.Company;
import org.kuali.rice.krad.datadictionary.validation.ErrorLevel;
import org.kuali.rice.krad.datadictionary.validation.constraint.CaseConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.MustOccurConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.PrerequisiteConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.ValidCharactersConstraint;
import org.kuali.rice.krad.datadictionary.validation.constraint.WhenConstraint;
import org.kuali.rice.krad.datadictionary.validation.processor.MustOccurConstraintProcessor;
import org.kuali.rice.krad.datadictionary.validation.result.ConstraintValidationResult;
import org.kuali.rice.krad.datadictionary.validation.result.DictionaryValidationResult;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.test.KRADTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DictionaryValidationServiceImplTest extends KRADTestCase{

	ClassPathXmlApplicationContext context;
	private DictionaryValidationServiceImpl service;
	DataDictionary dataDictionary = new DataDictionary();	
	
	protected AttributeDefinition street1Definition;
	protected AttributeDefinition street2Definition;
	protected AttributeDefinition stateDefinition;
	protected AttributeDefinition postalCodeDefinition;
	protected AttributeDefinition countryDefinition;
	protected DataObjectEntry addressEntry;
	
	
	
	private Address validLondonAddress = new Address("8129 Maiden Lane", "", "London", "", "SE1 0P3", "UK", null);
	private Address validUSAddress = new Address("893 Presidential Ave", "Suite 800", "Washington", "DC", "12031", "USA", null);
	private Address noStateUSAddress = new Address("893 Presidential Ave", "Suite 800", "Washington", "", "92342", "USA", null);
	private Address noZipNoCityUSAddress = new Address("893 Presidential Ave", "Suite 800", null, "DC", null, "USA", null);
	private Address validNonDCUSAddress = new Address("89 11th Street", "Suite 800", "Seattle", "WA", "", "USA", null);
	private Address invalidDCUSAddress = new Address("89 Presidential Ave", "Suite 800", "Washington", "DC", "12031", "USA", null);
	private Address invalidHKAddress = new Address("182 Lin Pai Road", "", "Hong Kong", "N.T.", "", "CN", null);
	
	
	@Before
	public void setUp() throws Exception {
		//super.setUp();
		
		context = new ClassPathXmlApplicationContext("classpath:DictionaryValidationServiceSpringBeans.xml");
		
		service = (DictionaryValidationServiceImpl)context.getBean("dictionaryValidationService");
	
		dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/krad/bo/datadictionary/DataDictionaryBaseTypes.xml");
        dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/kns/bo/datadictionary/DataDictionaryBaseTypes.xml");
		dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/krad/test/datadictionary/validation/Company.xml");
		dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/krad/test/datadictionary/validation/Address.xml");
		dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/krad/test/datadictionary/validation/Employee.xml");
		dataDictionary.addConfigFileLocation("classpath:org/kuali/rice/krad/test/datadictionary/validation/Person.xml");		

		dataDictionary.parseDataDictionaryConfigurationFiles(false);
		
		addressEntry = dataDictionary.getDataObjectEntry("org.kuali.rice.krad.datadictionary.validation.Address");
	}
	
	
	@Test
	public void testValidNonUSAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(validLondonAddress, "org.kuali.rice.kns.datadictionary.validation.MockAddress", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfErrors());
	}
	
    @Test
	public void testValidUSAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(validUSAddress, "org.kuali.rice.kns.datadictionary.validation.MockAddress", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfErrors());
	}
	
	@Test
	public void testInvalidUSAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(noStateUSAddress, "org.kuali.rice.kns.datadictionary.validation.MockAddress", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(1, dictionaryValidationResult.getNumberOfErrors());
	}
	
	@Test
	public void testValidNonDCAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(validNonDCUSAddress, "org.kuali.rice.krad.datadictionary.validation.Address", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfErrors());
	}
	
	@Test
	public void testInvalidDCAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(invalidDCUSAddress, "org.kuali.rice.krad.datadictionary.validation.Address", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(1, dictionaryValidationResult.getNumberOfErrors());
	}
	
	@Test
	public void testNoStateNoZipUSAddress() {
		DictionaryValidationResult dictionaryValidationResult = service.validate(noZipNoCityUSAddress, "org.kuali.rice.krad.datadictionary.validation.Address", addressEntry, true);
		
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
		Assert.assertEquals(1, dictionaryValidationResult.getNumberOfErrors());
		
		if (dictionaryValidationResult.getNumberOfErrors() > 0) {
	    	for (Iterator<ConstraintValidationResult> iterator = dictionaryValidationResult.iterator() ; iterator.hasNext() ;) {
	    		ConstraintValidationResult constraintValidationResult = iterator.next();
	    		if (constraintValidationResult.getStatus().getLevel() >= ErrorLevel.WARN.getLevel()) {
	    			// The top level error should be an occurs error
	    			Assert.assertEquals(ErrorLevel.ERROR, constraintValidationResult.getStatus());
	    			Assert.assertEquals("error.occurs", constraintValidationResult.getErrorKey());
	    			// It should have two children
	    			List<ConstraintValidationResult> children = constraintValidationResult.getChildren();
	    			Assert.assertNotNull(children);
	    			Assert.assertEquals(2, children.size());
	    			// The first child should have it's own child 
	    			ConstraintValidationResult child1 = children.get(0);
	    			ConstraintValidationResult child2 = children.get(1);
	    			
	    			Assert.assertEquals("error.requiresField", child1.getErrorKey());
	    			Assert.assertArrayEquals(new String[] { "postalCode" }, child1.getErrorParameters());
	    			
	    			List<ConstraintValidationResult> grandchildren = child2.getChildren();
	    			Assert.assertNotNull(grandchildren);
	    			Assert.assertEquals(2, grandchildren.size());
	    			ConstraintValidationResult grandchild1 = grandchildren.get(0);
	    			Assert.assertEquals(ErrorLevel.ERROR, grandchild1.getStatus());
	    			Assert.assertEquals("error.requiresField", grandchild1.getErrorKey());
	    			Assert.assertArrayEquals(new String[] { "city" }, grandchild1.getErrorParameters());
	    			ConstraintValidationResult grandchild2 = grandchildren.get(1);
	    			Assert.assertEquals(ErrorLevel.OK, grandchild2.getStatus());
	    			Assert.assertEquals(new MustOccurConstraintProcessor().getName(), grandchild2.getConstraintName());
	    		}
	    	}
    	}
	}
	
	
    @Test
    public void testSimpleCaseConstraints() throws IOException{
        DictionaryValidationResult dictionaryValidationResult = service.validate(invalidHKAddress, "org.kuali.rice.krad.datadictionary.validation.Address", addressEntry, true);
        
        Assert.assertEquals(0, dictionaryValidationResult.getNumberOfWarnings());
        Assert.assertEquals(1, dictionaryValidationResult.getNumberOfErrors());
    }

	@Test
	public void testRequiredNestedAttribute() throws IOException{	
		DataDictionaryService dataDictionaryService = new DataDictionaryServiceImpl(dataDictionary);
		service.setDataDictionaryService(dataDictionaryService);
		
		//Get object entries from dictionary
		DataObjectEntry addressEntry = dataDictionary.getDataObjectEntry("org.kuali.rice.krad.datadictionary.validation.Address");
        DataObjectEntry companyEntry = dataDictionary.getDataObjectEntry("org.kuali.rice.krad.datadictionary.validation.Company");
		
		//Validate object entries
		addressEntry.completeValidation();
		companyEntry.completeValidation();
		
		Company acmeCompany = new Company();
		
		//Validate empty Company object
        DictionaryValidationResult dictionaryValidationResult;		
		dictionaryValidationResult = service.validate(acmeCompany, "org.kuali.rice.krad.datadictionary.validation.Company",companyEntry, true);
		
		//Main address is required this should result in error
		Assert.assertEquals(1, dictionaryValidationResult.getNumberOfErrors());
		Iterator<ConstraintValidationResult> iterator = dictionaryValidationResult.iterator();
		ConstraintValidationResult constraintValidationResult = iterator.next();
		Assert.assertEquals("mainAddress", constraintValidationResult.getAttributePath());
		
		//Adding an invalid mainAddress for company 
		Address acmeMainAddress = new Address();
		acmeCompany.setMainAddress(acmeMainAddress);
		
		dictionaryValidationResult = service.validate(acmeCompany, "org.kuali.rice.krad.datadictionary.validation.Company",companyEntry, true);
		
		//This should result in missing country error
		Assert.assertEquals(2, dictionaryValidationResult.getNumberOfErrors());
		Iterator<ConstraintValidationResult> dictionaryValidationResultIterator = dictionaryValidationResult.iterator();
		constraintValidationResult = dictionaryValidationResultIterator.next();		
		Assert.assertEquals("mainAddress.country", constraintValidationResult.getAttributePath());
        constraintValidationResult = dictionaryValidationResultIterator.next();
        Assert.assertEquals("error.occurs", constraintValidationResult.getErrorKey());        
		
		//Set items to valid address
		acmeMainAddress.setCountry("US");
		acmeMainAddress.setPostalCode("11111");
		
		dictionaryValidationResult = service.validate(acmeCompany, "org.kuali.rice.krad.datadictionary.validation.Company",companyEntry, true);
		
		//This should result in no error
		Assert.assertEquals(0, dictionaryValidationResult.getNumberOfErrors());
		
	}
	
}
