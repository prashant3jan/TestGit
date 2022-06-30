package com.struts2.action;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

	public static String JPA_VERSION = "2.1";
    private String persistenceUnitName;
    private PersistenceUnitTransactionType transactionType
      = PersistenceUnitTransactionType.RESOURCE_LOCAL;
    private List<String> managedClassNames;
    private List<String> mappingFileNames = new ArrayList<>();
    private Properties properties;
    private DataSource jtaDataSource;
    private DataSource nonjtaDataSource;
    private List<ClassTransformer> transformers = new ArrayList<>();
    
    public PersistenceUnitInfoImpl(
      String persistenceUnitName, 
      List<String> managedClassNames, 
      Properties properties) {
        this.persistenceUnitName = persistenceUnitName;
        this.managedClassNames = managedClassNames;
        this.properties = properties;
    }

	@Override
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	@Override
	public String getPersistenceProviderClassName() {
		return HibernatePersistenceProvider.class.getName();
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return transactionType;
	}

	@Override
	public DataSource getJtaDataSource() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 public PersistenceUnitInfoImpl setJtaDataSource(
	            DataSource jtaDataSource) {
	        this.jtaDataSource = jtaDataSource;
	        this.nonjtaDataSource = null;
	        transactionType = PersistenceUnitTransactionType.JTA;
	        return this;
	    }

	@Override
	public DataSource getNonJtaDataSource() {
		return jtaDataSource;
	}
	
	 public PersistenceUnitInfoImpl setNonJtaDataSource(
	            DataSource nonJtaDataSource) {
	        this.nonjtaDataSource = nonJtaDataSource;
	        this.jtaDataSource = null;
	        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
	        return this;
	    }

	@Override
	public List<String> getMappingFileNames() {
		return mappingFileNames;
	}

	@Override
	public List<URL> getJarFileUrls() {
		return Collections.emptyList();
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		return null;
	}

	@Override
	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return false;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.UNSPECIFIED;
	}

	@Override
	public ValidationMode getValidationMode() {
		return ValidationMode.AUTO;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return JPA_VERSION;
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		
		
	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return null;
	}
    
    

}
