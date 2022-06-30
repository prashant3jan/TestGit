package com.struts2.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import java.text.ParseException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import com.struts2.model.DebtorPassbook;
import com.struts2.model.Document;
import com.struts2.model.Member;
import com.struts2.model.MemberBankInfo;

import oracle.sql.BLOB;

public class MemberManager extends HibernateUtil {
	public Member getMemDetByMemNo(String memNum) {
		List<Member> memDetList = new ArrayList<Member>();
		Session session = HibernateUtil.getHibernateSession();
		String hql = "SELECT m.memNum, m.memDate, m.memImageData, m.applicantName, m.applicantDob, m.applicantSex, m.applicantFather, m.applicantMother, m.applicantOccupation, m.applicantAddress, m.applicantCity, m.applicantState, m.applicantPin, m.applicantPan, m.applicantMobile, m.nomineeName, m.nomineeAge, m.relnWithNominee, m.nomineeAddress, m.memFeeFig, m.memFeeWords, m.authSigntryData, m.memSignData, m.grntrDet, m.grntrSignData, m.empSignData from Member m where m.memNum = :memNum";
		Query query = session.createQuery(hql);
		query.setParameter("memNum", memNum);
		memDetList = (List<Member>) query.getResultList();
		// DebtorPassbook dp = loanDetList.get(0);
		Iterator iterator = memDetList.iterator();
		System.out.println("memDetList.size()db:" + memDetList.size());
		Member memberBean = null;
		Object[] results = null;
		while (iterator.hasNext()) {
			results = (Object[]) iterator.next();
			System.out.println("results.length" + results.length);
			memberBean = new Member();
			if(results[0]!=null){
			System.out.println("memNum" + results[0].toString());
			memberBean.setMemNum(results[0].toString());
			}
			try {
				if(results[1]!=null){
				Date memDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(results[1].toString());
				System.out.println("dateLoanDate" + memDate);
				memberBean.setMemDate(memDate);
				}
				if(results[2]!=null){
				byte[] memImageByteArray = (byte[]) results[2];
				String base64MemImage = Base64.getEncoder().encodeToString(memImageByteArray);
				memberBean.setMemImageData(memImageByteArray);
				memberBean.setBase64MemImage(base64MemImage);
				}
				if(results[3]!=null){
				memberBean.setApplicantName(results[3].toString());
				}
				if(results[4]!=null){
				Date appDob = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(results[4].toString());
				System.out.println("dateLoanDate" + appDob);
				memberBean.setApplicantDob(appDob);
				}
				if(results[5]!=null){
				memberBean.setApplicantSex(results[5].toString());
				}
				if(results[6]!=null){
				memberBean.setApplicantFather(results[6].toString());
				}
				if(results[7]!=null){
				memberBean.setApplicantMother(results[7].toString());
				}
				if(results[8]!=null){
				memberBean.setApplicantOccupation(results[8].toString());
				}
				if(results[9]!=null){
				memberBean.setApplicantAddress(results[9].toString());
				}
				if(results[10]!=null){
				memberBean.setApplicantCity(results[10].toString());
				}
				if(results[11]!=null){
				memberBean.setApplicantState(results[11].toString());
				}
				if(results[12]!=null){
				memberBean.setApplicantPin(Integer.parseInt(results[12].toString()));
				}
				if(results[13]!=null){
				memberBean.setApplicantPan(results[13].toString());
				}
				if(results[14]!=null){
				memberBean.setApplicantMobile(results[14].toString());
				}
				if(results[15]!=null){
				memberBean.setNomineeName(results[15].toString());
				}
				if(results[16]!=null){
				memberBean.setNomineeAge(Integer.parseInt(results[16].toString()));
				}
				if(results[17]!=null){
				memberBean.setRelnWithNominee(results[17].toString());
				}
				if(results[18]!=null){
				memberBean.setNomineeAddress(results[18].toString());
				}
				if(results[19]!=null){
				memberBean.setMemFeeFig(results[19].toString());
				}
				if(results[20]!=null){
				memberBean.setMemFeeWords(results[20].toString());
				}
				if(results[21]!=null){
				byte[] authSigntryByteArray = (byte[]) results[21];
				String base64authSigntry = Base64.getEncoder().encodeToString(authSigntryByteArray);
				memberBean.setMemImageData(authSigntryByteArray);
				memberBean.setBase64authSigntry(base64authSigntry);
				}
				if(results[22]!=null){
				byte[] memSignByteArray = (byte[]) results[22];
				String base64MemSign = Base64.getEncoder().encodeToString(memSignByteArray);
				memberBean.setMemSignData(memSignByteArray);
				memberBean.setBase64MemSign(base64MemSign);
				}
				if(results[23]!=null){
				memberBean.setGrntrDet(results[23].toString());
				}
				if(results[24]!=null){
				byte[] grntrSignByteArray = (byte[]) results[24];
				String base64GrntrSign = Base64.getEncoder().encodeToString(grntrSignByteArray);
				memberBean.setGrntrSignData(grntrSignByteArray);
				memberBean.setBase64GrntrSign(base64GrntrSign);
				}
				if(results[25]!=null){
				byte[] empSignByteArray = (byte[]) results[25];
				String base64EmpSign = Base64.getEncoder().encodeToString(empSignByteArray);
				memberBean.setEmpSignData(empSignByteArray);
				memberBean.setBase64EmpSign(base64EmpSign);
				}
			} catch (ParseException pe) {
				pe.printStackTrace();
			}catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
		return memberBean;
	}

	public void add(Member member) {
		System.out.println("inside add method");
		Session session = HibernateUtil.getHibernateSession();
		System.out.println("session object created " + session);
		Transaction tx = session.beginTransaction();
		System.out.println("transaction object created" + tx);
		session.save(member);
		System.out.println("save executed");
		tx.commit();
		System.out.println("transaction committed");
		session.close();
		System.out.println("session closed");
	}

	public void addMemBankDet(MemberBankInfo memberBankInfo) {
		System.out.println("inside add method");
		Session session = HibernateUtil.getHibernateSession();
		System.out.println("session object created " + session);
		Transaction tx = session.beginTransaction();
		System.out.println("transaction object created" + tx);
		session.save(memberBankInfo);
		System.out.println("save executed");
		tx.commit();
		System.out.println("transaction committed");
		session.close();
		System.out.println("session closed");
	}

	public Member delete(String membership_no) {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("membership_no" + membership_no);
		Member member = (Member) session.load(Member.class, membership_no);
		if (null != member) {
			session.delete(member);
		}
		tx.commit();
		session.close();
		return member;
	}

	public void deleteMemBnkInfo(String membership_no) {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("membership_no" + membership_no);
		MemberBankInfo memberBankInfo = (MemberBankInfo) session.load(MemberBankInfo.class, membership_no);
		if (memberBankInfo != null) {
			session.delete(memberBankInfo);
		}
		tx.commit();
		session.close();
	}

	public List<Member> memberList() {
		SessionFactory factory = new Configuration().configure().buildSessionFactory();
		 Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		List<Member> memberList = null;
		try {

			memberList = (List<Member>) session.createQuery("from Member").list();

		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}catch(Exception e){
			e.printStackTrace();
		}
		return memberList;
	}

	public List<MemberBankInfo> memberBankInfoList() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<MemberBankInfo> memberBankInfoList = null;
		try {

			memberBankInfoList = (List<MemberBankInfo>) session.createQuery("from MemberBankInfo").list();

		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}
		return memberBankInfoList;
	}

	public List<Document> listDocs() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<Document> documents = null;
		try {

			documents = (List<Document>) session.createQuery("from Document").list();
			// documents = (List<Document>)session.createQuery("select a from
			// Member a where
			// a.document.name=:value").setParameter("value","name").list();
		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}
		session.getTransaction().commit();
		return documents;
	}
}
