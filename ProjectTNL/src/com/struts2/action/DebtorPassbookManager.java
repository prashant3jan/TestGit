package com.struts2.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.struts2.model.CustLoanBean;
import com.struts2.model.DebtorPassbook;

public class DebtorPassbookManager {
	private Double installment;

	public DebtorPassbook getDebtorPassbookDet(String loanAccno) {
		List<DebtorPassbook> loanDetList = new ArrayList<DebtorPassbook>();
		Session session = HibernateUtil.getHibernateSession();
		String hql = "SELECT l.loanAccno, l.applicantName, l.applicantFather, l.applicantMother,l.applicantAddress, l.applicantCity, l.applicantState, l.applicantPin, l.repayPeriod, l.pmtFrequency, l.memSign, l.empSign, c.debitTotLoanWithInterest, c.dateOfLoan, b.chequeNo from CustLoanBean l, CustPmtBean c, CompanyBankAccount b where l.loanAccno = c.custLoanDet.loanAccno and l.loanAccno = b.loan.loanAccno and l.loanAccno = :loanAccno";
		Query query = session.createQuery(hql);
		query.setParameter("loanAccno", loanAccno);
		loanDetList = (List<DebtorPassbook>) query.getResultList();
		//DebtorPassbook dp = loanDetList.get(0);
		Iterator iterator = loanDetList.iterator();
		System.out.println("loanDetList.size()db:"+loanDetList.size());
		DebtorPassbook debtorPassbook = null;
		Object[] results=null;
		while (iterator.hasNext()){
		results = (Object[]) iterator.next();
		System.out.println("results.length"+results.length) ;
		debtorPassbook = new DebtorPassbook();
		debtorPassbook.setLoanAccno(results[0].toString());
		System.out.println("loanAccno"+results[0].toString());
		debtorPassbook.setApplicantName(results[1].toString());
		System.out.println("applicantName"+results[1].toString());
		debtorPassbook.setApplicantFather(results[2].toString());
		debtorPassbook.setApplicantMother(results[3].toString());
		debtorPassbook.setApplicantAddress(results[4].toString());
		debtorPassbook.setApplicantCity(results[5].toString());
		debtorPassbook.setApplicantState(results[6].toString());
		debtorPassbook.setApplicantPin(Long.parseLong(results[7].toString()));
		Double repayperiod = Double.parseDouble(results[8].toString());
		debtorPassbook.setRepayPeriod(Double.parseDouble(results[8].toString()));
		String paymentFrequency = results[9].toString();
		debtorPassbook.setPmtFrequency(results[9].toString());
		debtorPassbook.setMemSign((File) results[10]);
		debtorPassbook.setEmpSign((File)results[11]);
		debtorPassbook.setDebitloanGvnByComp(Double.parseDouble(results[12].toString()));
		Double debitLoanGvnByComp = Double.parseDouble(results[12].toString());
		if(paymentFrequency.equals("daily")){
			installment = debitLoanGvnByComp /(repayperiod *30);
		}else if(paymentFrequency.equals("weekly")){
			installment = debitLoanGvnByComp /(repayperiod *4);
		}else if(paymentFrequency.equals("monthly")){
			installment = debitLoanGvnByComp /(repayperiod *1);
		}
		System.out.println("installment"+installment);
		debtorPassbook.setInstallment(installment);
		debtorPassbook.setDateOfLoan((Date)results[13]);
		debtorPassbook.setChequeNo(results[14].toString());
		System.out.println("check no"+results[14].toString());
		}
		return debtorPassbook;
	}
}