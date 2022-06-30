package com.struts2.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.hibernate.Query;
import org.hibernate.Session;

import com.struts2.model.DebtorPassbook;
import com.struts2.model.PndngInstlmntRprtBean;
import com.struts2.model.PndngInstlmntRprtDTO;

public class PndngInstlmntRprtManager extends HibernateUtil {
	private Double installment;

	public List<PndngInstlmntRprtBean> getPendingInstlmntRprt() {
		List<PndngInstlmntRprtBean> pndngInstlmntRprtBeanList = new ArrayList<PndngInstlmntRprtBean>();
		List<PndngInstlmntRprtDTO> pndngInstlmntRprtDTOList = new ArrayList<PndngInstlmntRprtDTO>();
		Session session = HibernateUtil.getHibernateSession();
		String hql = "SELECT l.loanAccno, l.applicantName, l.applicantAddress, l.applicantCity, l.applicantState, l.applicantPin, l.applicantMobile, l.repayPeriod, l.pmtFrequency, c.debitTotLoanWithInterest,  max(c.creditPmtGvnByCust), c.dateOfLoan  from CustLoanBean l, CustPmtBean c where l.loanAccno = c.custLoanDet.loanAccno group by l.loanAccno, l.applicantName, l.applicantAddress, l.applicantCity, l.applicantState, l.applicantPin, l.applicantMobile, l.repayPeriod, l.pmtFrequency, c.debitTotLoanWithInterest, c.dateOfLoan";
		Query query = session.createQuery(hql);
		pndngInstlmntRprtDTOList = (List<PndngInstlmntRprtDTO>) query.getResultList();
		Iterator iterator = pndngInstlmntRprtDTOList.iterator();
		System.out.println("pndngInstlmntRprtDTOList.size()db:" + pndngInstlmntRprtDTOList.size());
		DebtorPassbook debtorPassbook = null;
		Object[] results = null;
		while (iterator.hasNext()) {
			results = (Object[]) iterator.next();
			System.out.println("results.length" + results.length);
			PndngInstlmntRprtBean pndngInstlmntRprtBean = new PndngInstlmntRprtBean();
			pndngInstlmntRprtBean.setLoanAccno(results[0].toString());
			System.out.println("loanAccno" + results[0].toString());
			pndngInstlmntRprtBean.setApplicantName(results[1].toString());
			System.out.println("applicantName" + results[1].toString());
			pndngInstlmntRprtBean.setApplicantAddress(results[2].toString());
			pndngInstlmntRprtBean.setApplicantCity(results[3].toString());
			pndngInstlmntRprtBean.setApplicantState(results[4].toString());
			pndngInstlmntRprtBean.setApplicantPin(Long.parseLong(results[5].toString()));
			pndngInstlmntRprtBean.setApplicantMobile(results[6].toString());
			Double repayperiod = Double.parseDouble(results[7].toString());
			System.out.println("repayperiod"+repayperiod);
			Double repayPeriodInDays=repayperiod*30;
			pndngInstlmntRprtBean.setRepayPeriod(repayperiod);
			String pmtFrequency = results[8].toString();
			System.out.println("pmtFrequency"+pmtFrequency);
			pndngInstlmntRprtBean.setPmtFrequency(results[8].toString());
			double totLoanWithInterest = Double.parseDouble(results[9].toString());
			System.out.println("totLoanWithInterest"+totLoanWithInterest);
			pndngInstlmntRprtBean.setTotLoanWithInterest(Double.parseDouble(results[9].toString()));
			double totPmtGvnByCustTillDate = Double.parseDouble(results[10].toString());
			System.out.println("totPmtGvnByCustTillDate"+totPmtGvnByCustTillDate);
			pndngInstlmntRprtBean.setTotPmtGvnByCustTillDate(totPmtGvnByCustTillDate);
			String stringLoanDate = results[11].toString();
			try {
				Date loanDate = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(stringLoanDate);
				System.out.println("dateLoanDate" + loanDate);
				pndngInstlmntRprtBean.setDateOfLoan(loanDate);
				String loanDate1 = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(loanDate);
				Date currentDate = new Date();
				String currentDate1 = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(currentDate);
				Date currentDate2 = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(currentDate1);
				System.out.println("currentDate2" + currentDate2);
				pndngInstlmntRprtBean.setCurrentDate(currentDate2);
				LocalDate dateBefore = LocalDate.parse(loanDate1);
				LocalDate dateAfter = LocalDate.parse(currentDate1);
				// calculating number of days in between
				long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
				if(noOfDaysBetween >repayPeriodInDays){
					pndngInstlmntRprtBean.setLoanStatus("expired");
				}else if(totLoanWithInterest>totPmtGvnByCustTillDate){
					pndngInstlmntRprtBean.setLoanStatus("unpaid");
				}else if(totLoanWithInterest==totPmtGvnByCustTillDate){
					pndngInstlmntRprtBean.setLoanStatus("paid");
				}
				System.out.println("noOfDaysBetween" + noOfDaysBetween);
				long totNoOfInstlmntDueTillDate = 0;
				
				if (pmtFrequency.equals("weekly")) {
					if(pndngInstlmntRprtBean.getLoanStatus()=="expired"){
						totNoOfInstlmntDueTillDate=0;
					}else{
					totNoOfInstlmntDueTillDate = noOfDaysBetween * 4 / 30;
					}
				} else if (pmtFrequency.equals("tendays")) {
					if(pndngInstlmntRprtBean.getLoanStatus()=="expired"){
						totNoOfInstlmntDueTillDate=0;
					}else{
					totNoOfInstlmntDueTillDate = noOfDaysBetween * 3 / 30;
					}
				} else if (pmtFrequency.equals("fifteendays")) {
					if(pndngInstlmntRprtBean.getLoanStatus()=="expired"){
						totNoOfInstlmntDueTillDate=0;
					}else{
					totNoOfInstlmntDueTillDate = noOfDaysBetween * 2 / 30;
					}
				}
				
				System.out.println("totNoOfInstlmntDueTillDate" + totNoOfInstlmntDueTillDate);
				pndngInstlmntRprtBean.setTotNoOfInstlmntDueTillDate(totNoOfInstlmntDueTillDate);
				Double instlmntAmt = 0.0;
				if (pmtFrequency.equals("weekly")) {
					instlmntAmt = totLoanWithInterest / (repayperiod * 4);
				} else if (pmtFrequency.equals("tendays")) {
					instlmntAmt = totLoanWithInterest / (repayperiod * 3);
				} else if (pmtFrequency.equals("fifteendays")) {
					instlmntAmt = totLoanWithInterest / (repayperiod * 2);
				}
				System.out.println("instlmntAmt" + instlmntAmt);
				pndngInstlmntRprtBean.setInstlmntAmt(instlmntAmt);
				Double totPmtDueTillDate=0.0;
				if(pndngInstlmntRprtBean.getLoanStatus()=="expired"){
					totPmtDueTillDate = totPmtDueTillDate;
				}else{
					totPmtDueTillDate = instlmntAmt * totNoOfInstlmntDueTillDate;
				}
				System.out.println("totPmtDueTillDate" + totPmtDueTillDate);
				pndngInstlmntRprtBean.setTotPmtDueTillDate(totPmtDueTillDate);
				Double outstandingBalance = totPmtDueTillDate - totPmtGvnByCustTillDate;
				System.out.println("outstandingBalance" + outstandingBalance);
				pndngInstlmntRprtBean.setOutstandingBalance(outstandingBalance);
				pndngInstlmntRprtBeanList.add(pndngInstlmntRprtBean);
			} catch (java.text.ParseException pe) {
				pe.printStackTrace();
			}
		}
		return pndngInstlmntRprtBeanList;
	}

}
