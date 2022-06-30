package com.struts2.action;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.struts2.model.MemberBankInfo;
import com.struts2.model.PromisaryBean;

public class PromisaryManager extends HibernateUtil {
	public void add(PromisaryBean promissaryBean) {
		System.out.println("inside add method");
		Session session = HibernateUtil.getHibernateSession();
		System.out.println("session object created "+ session);
		Transaction tx = session.beginTransaction();
		System.out.println("transaction object created"+tx);
		session.save(promissaryBean);
		System.out.println("save executed");
		tx.commit();
		System.out.println("transaction committed");
		session.close();
		System.out.println("session closed");
	}
	public List<PromisaryBean> getPromisaryDet() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<PromisaryBean> promissaryList = null;
		try {
			
			promissaryList = (List<PromisaryBean>)session.createQuery("from PromisaryBean").list();
			
		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}
		return promissaryList;
	}
	public void delete(int prmssryId){
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("prmssryId"+prmssryId);
		PromisaryBean promissaryBean= (PromisaryBean) session.load(PromisaryBean.class, prmssryId);
		if(promissaryBean !=null){
			session.delete(promissaryBean);
		}
		tx.commit();
		session.close();
	}

}
