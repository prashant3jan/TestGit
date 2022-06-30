package com.struts2.action;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.struts2.model.PromisaryBean;
import com.struts2.model.ThumbImprssnBean;

public class ThumbImprssnManager extends HibernateUtil {
	public void add(ThumbImprssnBean thumbImprssnBean) {
		System.out.println("inside add method");
		Session session = HibernateUtil.getHibernateSession();
		System.out.println("session object created "+ session);
		Transaction tx = session.beginTransaction();
		System.out.println("transaction object created"+tx);
		session.save(thumbImprssnBean);
		System.out.println("save executed");
		tx.commit();
		System.out.println("transaction committed");
		session.close();
		System.out.println("session closed");
	}
	public List<ThumbImprssnBean> getThumbImprssnDet() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<ThumbImprssnBean> thumbImprssnList = null;
		try {
			
			thumbImprssnList = (List<ThumbImprssnBean>)session.createQuery("from ThumbImprssnBean").list();
			
		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}
		return thumbImprssnList;
	}
	public void delete(int thumbImprssnId){
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("thumbImprssnId"+thumbImprssnId);
		ThumbImprssnBean thumbImprssnBean= (ThumbImprssnBean) session.load(ThumbImprssnBean.class, thumbImprssnId);
		if(thumbImprssnBean !=null){
			session.delete(thumbImprssnBean);
		}
		tx.commit();
		session.close();
	}



}
