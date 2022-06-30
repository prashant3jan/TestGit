package com.struts2.action;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.struts2.model.CompanyBankAccount;


public class BankPassbookManager {
	public List<CompanyBankAccount> list(){
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<CompanyBankAccount> transactionList = null;
			try{
				transactionList = (List<CompanyBankAccount>)session.createQuery("from CompanyBankAccount").list();
				System.out.println("present size of list"+transactionList.size());
			}catch(HibernateException e){
				e.printStackTrace();
				tx.rollback();
			}
		
		return transactionList;
		
	}
	
	public CompanyBankAccount add(CompanyBankAccount compBankAccBean){
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		session.save(compBankAccBean);
		tx.commit();
		session.close();
		return compBankAccBean;
	}
	
	public CompanyBankAccount delete(int id) {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("membership_no"+id);
		CompanyBankAccount transaction = (CompanyBankAccount) session.load(CompanyBankAccount.class, id);
		if(null != transaction) {
			session.delete(transaction);
		}
		tx.commit();
		session.close();
		return transaction;
	}

}
