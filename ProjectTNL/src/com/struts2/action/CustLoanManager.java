package com.struts2.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.struts2.model.Collateral;
import com.struts2.model.CollateralGvn;
import com.struts2.model.CustLoanBean;
import com.struts2.model.CustLoanBeanDTO;
import com.struts2.model.DebtorPassbook;
import com.struts2.model.LoanDocs;
import com.struts2.model.Member;
import com.struts2.model.PndngInstlmntRprtDTO;

import oracle.sql.BLOB;

public class CustLoanManager extends HibernateUtil {
	public CustLoanBeanDTO getLoanDetByLoanAccno(String loanAccno) {
		List<CustLoanBeanDTO> custLoanDTOList = new ArrayList<CustLoanBeanDTO>();
		Session session = HibernateUtil.getHibernateSession();
		System.out.println("loanAccno being passed to query"+loanAccno);
		String loanaccno = loanAccno.trim();
		String hql = "SELECT l.member.memNum, l.loanAccno, l.dateOfLoan, l.repayPeriod, l.pmtFrequency, l.depositMode, l.chqDate, l.chqNum, l.bankName, l.bankBrnchDet, l.appImageData, l.applicantName, l.applicantDob, applicantSex, l.applicantFather, l.applicantMother, l.applicantOccupation, l.applicantAddress, l.applicantCity, l.applicantState, l.applicantPin, l.applicantPan, l.applicantMobile, l.nomineeName, l.nomineeAge, l.relnWithNominee, l.nomineeAddress, l.memSignData, l.grntrDet, l.grntrSignData, l.empSignData from CustLoanBean l where l.loanAccno=:loanaccno";
		//String hql = "SELECT l.loanAccno from CustLoanBean l where l.loanAccno=:loanaccno";
		Query query = session.createQuery(hql);
		System.out.println("query"+query);
		query.setParameter("loanaccno", loanaccno);
		custLoanDTOList = (List<CustLoanBeanDTO>) query.getResultList();
		System.out.println("custLoanDTOList.size()" + query.getResultList().size());
		Iterator iterator = custLoanDTOList.iterator();
		CustLoanBeanDTO custLoanBeanDto = null;
		Object[] results = null;
		while (iterator.hasNext()) {
			results = (Object[]) iterator.next();
			System.out.println("results.length" + results.length);
			try {
				custLoanBeanDto = new CustLoanBeanDTO();
				if(results[0].toString()!= null){
				custLoanBeanDto.setMemNum(results[0].toString());
				System.out.println("memNum" + results[0].toString());
				}
				if(results[1].toString() != null){
				custLoanBeanDto.setLoanAccno(results[1].toString());
				}
				if(results[2].toString()!=null){
				Date dateOfLoan = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(results[2].toString());
				System.out.println("dateLoanDate" + dateOfLoan);
				custLoanBeanDto.setDateOfLoan(dateOfLoan);
				}
				if(results[3]!=null){
				custLoanBeanDto.setRepayPeriod(Double.parseDouble(results[3].toString()));
				}
				if(results[4]!=null){
				custLoanBeanDto.setPmtFrequency(results[4].toString());
				}
				if(results[5]!=null){
				custLoanBeanDto.setDepositMode(results[5].toString());
				}
				if(results[6]!=null){
				Date chqDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(results[6].toString());
				System.out.println("chqDate" + chqDate);
				custLoanBeanDto.setChqDate(chqDate);
				}
				if(results[7]!=null){
				custLoanBeanDto.setChqNum(results[7].toString());
				}
				if(results[8]!=null){
				custLoanBeanDto.setBankName(results[8].toString());
				}
				if(results[9]!=null){
				custLoanBeanDto.setBankBrnchDet(results[9].toString());
				}
				if(results[10]!=null){
				byte[] appImageByteArray = (byte[]) results[10];
				String base64AppImage = Base64.getEncoder().encodeToString(appImageByteArray);
				custLoanBeanDto.setAppImageData(appImageByteArray);
				custLoanBeanDto.setBase64AppImage(base64AppImage);
				}
				if(results[11]!=null){
				custLoanBeanDto.setApplicantName(results[11].toString());
				}
				if(results[12]!=null){
				Date appDob = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(results[12].toString());
				System.out.println("dateLoanDate" + appDob);
				custLoanBeanDto.setApplicantDob(appDob);
				}
				if(results[13]!=null){
				custLoanBeanDto.setApplicantSex(results[13].toString());
				}
				if(results[14]!=null){
				custLoanBeanDto.setApplicantFather(results[14].toString());
				}
				if(results[15]!=null){
				custLoanBeanDto.setApplicantMother(results[15].toString());
				}
				if(results[16]!=null){
				custLoanBeanDto.setApplicantOccupation(results[16].toString());
				}
				if(results[17]!=null){
				custLoanBeanDto.setApplicantAddress(results[17].toString());
				}
				if(results[18]!=null){
				custLoanBeanDto.setApplicantCity(results[18].toString());
				}
				if(results[19]!=null){
				custLoanBeanDto.setApplicantState(results[19].toString());
				}
				if(results[20]!=null){
				custLoanBeanDto.setApplicantPin(Integer.parseInt(results[20].toString()));
				}
				if(results[21]!=null){
				custLoanBeanDto.setApplicantPan(results[21].toString());
				}
				if(results[22]!=null){
				custLoanBeanDto.setApplicantMobile(results[22].toString());
				}
				if(results[23]!=null){
				custLoanBeanDto.setNomineeName(results[23].toString());
				}
				if(results[24]!=null){
				custLoanBeanDto.setNomineeAge(Integer.parseInt(results[24].toString()));
				}
				if(results[25]!=null){
				custLoanBeanDto.setRelnWithNominee(results[25].toString());
				}
				if(results[26]!=null){
				custLoanBeanDto.setNomineeAddress(results[26].toString());
				}
				if(results[27]!=null){
				byte[] memSignByteArray = (byte[]) results[27];
				String base64MemSign = Base64.getEncoder().encodeToString(memSignByteArray);
				custLoanBeanDto.setAppImageData(memSignByteArray);
				custLoanBeanDto.setBase64MemSign(base64MemSign);
				}
				if(results[28]!=null){
				custLoanBeanDto.setGrntrDet(results[28].toString());
				}
				if(results[29]!=null){
				byte[] grntrSignByteArray = (byte[]) results[29];
				String base64GrntrSign = Base64.getEncoder().encodeToString(grntrSignByteArray);
				custLoanBeanDto.setGrntrSignData(grntrSignByteArray);
				custLoanBeanDto.setBase64GrntrSign(base64GrntrSign);
				}
				if(results[30]!=null){
				byte[] empSignByteArray = (byte[]) results[30];
				String base64EmpSign = Base64.getEncoder().encodeToString(empSignByteArray);
				custLoanBeanDto.setEmpSignData(empSignByteArray);
				custLoanBeanDto.setBase64EmpSign(base64EmpSign);
				}
			} catch (ParseException pe) {
				pe.printStackTrace();
			}catch(NullPointerException npe){
				npe.printStackTrace();
			}
			finally{
				session.close();
			}
		}
		return custLoanBeanDto;
	}

	public List<Collateral> listColl() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<Collateral> documents = null;
		try {

			documents = (List<Collateral>) session.createQuery("from Collateral").list();
		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}

		return documents;
	}

	public List<CustLoanBean> loanList() {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		List<CustLoanBean> loanList = null;
		try {

			loanList = (List<CustLoanBean>) session.createQuery("from CustLoanBean").list();

		} catch (HibernateException e) {
			e.printStackTrace();
			tx.rollback();
		}
		return loanList;
	}

	public CustLoanBean add(CustLoanBean loan) {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(loan);
		tx.commit();
		session.close();
		return loan;
	}

	public void delete(String loanAccno) {
		Session session = HibernateUtil.getHibernateSession();
		Transaction tx = session.beginTransaction();
		System.out.println("loanAccno" + loanAccno);
		CustLoanBean loan = (CustLoanBean) session.load(CustLoanBean.class, loanAccno);
		if (null != loan) {
			session.delete(loan);
		}
		tx.commit();
		session.close();
	}

}
