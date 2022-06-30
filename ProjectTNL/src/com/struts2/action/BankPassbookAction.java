package com.struts2.action;


import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.struts2.model.CompanyBankAccount;
import com.opensymphony.xwork2.ActionSupport;


public class BankPassbookAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	private BankPassbookManager passbookManager;
	private List<CompanyBankAccount> transacList;
	private CompanyBankAccount transacBean;
	private long balance;
	public int tranId;
	public int getTranId() {
		return tranId;
	}


	public void setTranId(int tranId) {
		this.tranId = tranId;
	}

	private long totalCredit;
	private long totalCreditNew;
	private long totalDebit;
	private long totalDebitNew;
	
	public BankPassbookAction(){
		passbookManager = new BankPassbookManager();
	}


	public void setBalance(long balance) {
		this.balance = balance;
	}
	public long getBalance(){
		for(int i=0; i< transacList.size();i++){
			totalCredit += transacList.get(i).getCreditAmountFig();
			totalDebit +=transacList.get(i).getDebitAmount();
		}
		balance = totalCredit - totalDebit;
		return balance;
		
	}
	
	public String execute(){
		transacList= passbookManager.list();
		System.out.println("execute called");
		return "success";
	}
	
	
	
	public String addTran(){
		try{
			transacList= passbookManager.list();
			System.out.println("transacList.size()"+transacList.size());
			for(int i=0; i< transacList.size();i++){
				if(transacList.get(i).getCreditAmountFig() != null){
				totalCredit += transacList.get(i).getCreditAmountFig();
				System.out.println("totalCredit"+totalCredit);
				}
				if(transacList.get(i).getDebitAmount() != null){
				totalDebit +=transacList.get(i).getDebitAmount();
				System.out.println("totalDebit"+totalDebit);
			}
			
			}
			
			CompanyBankAccount compbankacc= this.getTransacBean();
			if(compbankacc.getDebitAmount()!=null){
				totalDebitNew = totalDebit + compbankacc.getDebitAmount();
				System.out.println("totalDebitNew"+totalDebitNew);
				//compbankacc.setTotalDebit(totalDebitNew);
			}else{
				totalDebitNew = totalDebit;
				//compbankacc.setTotalDebit(totalDebitNew);
				System.out.println("totalDebitNew"+totalDebitNew);
			}
			if(compbankacc.getCreditAmountFig() != null){
				totalCreditNew = totalCredit + compbankacc.getCreditAmountFig();
				System.out.println("totalCreditNew"+totalCreditNew);
				//compbankacc.setTotalCredit(totalCreditNew);
			}else{
				totalCreditNew = totalCredit;
				//compbankacc.setTotalCredit(totalCreditNew);
				System.out.println("totalCreditNew"+totalCreditNew);
			}
			compbankacc.setTotalDebit(totalDebitNew);
			compbankacc.setTotalCredit(totalCreditNew);
			balance = totalCreditNew - totalDebitNew;
			System.out.println("balance"+balance);
			compbankacc.setBalance(balance);
			passbookManager.add(compbankacc);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			this.transacList = passbookManager.list();
			return "success";
	}
	
	public String addCreditTran(){
		try{
			passbookManager.add(getTransacBean());
			}catch(Exception e){
				e.printStackTrace();
			}
			this.transacList = passbookManager.list();
			return "success";
	}
	public String tranDelete(){
		passbookManager.delete(tranId);
		transacList= passbookManager.list();
		return "success";
	}
	public List<CompanyBankAccount> getTransacList() {
		return transacList;
	}
	public void setTransacList(List<CompanyBankAccount> transacList) {
		this.transacList = transacList;
	}

	public CompanyBankAccount getTransacBean() {
		return transacBean;
	}

	public void setTransacBean(CompanyBankAccount transacBean) {
		this.transacBean = transacBean;
	}

}
