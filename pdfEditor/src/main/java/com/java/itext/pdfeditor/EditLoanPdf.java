package com.java.itext.pdfeditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

public class EditLoanPdf {
	private static String INPUTFILE = "e://temp//loan//LoanForm.pdf";
	private static String OUTPUTFILE = "e://temp//loan//LoanFormOutput.pdf";
	private static String DATAFILE = "e://temp//loan//inputLoan.txt";
	public static final String IMGSRC = "e://temp//loan//image.jpg";

	PdfReader reader;
	PdfWriter writer;

	protected String[] arguments;

	public EditLoanPdf(PdfReader _reader, PdfWriter _writer) {
		this.reader = _reader;
		this.writer = _writer;
	}

	public EditLoanPdf() {
	}

	public static void main(String[] args) throws DocumentException, IOException {
		File file = new File(OUTPUTFILE);
		file.getParentFile().mkdirs();
		new EditMemberPdf().manipulatePdf(INPUTFILE, OUTPUTFILE, DATAFILE, IMGSRC);
	}

	public void manipulatePdf(String src, String dest, String data, String imgSrc) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader(data));
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(":", 2);
			if (parts.length >= 2) {
				String key = parts[0];
				String value = parts[1];
				map.put(key, value);
			} else {
				System.out.println("ignoring line: " + line);
			}
		}
		for (String key : map.keySet()) {
			System.out.println(key + ":" + map.get(key));
		}
		System.out.println("input printing finished");
		PdfReader reader = new PdfReader(src);
		PdfWriter writer = new PdfWriter(dest);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);
		PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, true);
		Map<String, PdfFormField> fields = acroForm.getFormFields();
		for (String fldName : fields.keySet()) {
			System.out.println(fldName + ": " + fields.get(fldName).getValueAsString());
		}
		System.out.println("fields_printing_finished");
		PdfButtonFormField button = (PdfButtonFormField) acroForm.getField("mem_image");
		button.setImage(IMGSRC);
		fields.get("mem_no").setValue(map.get("mem_no"));
		fields.get("mem_date").setValue(map.get("mem_date"));
		fields.get("app_name").setValue(map.get("app_name"));
		fields.get("app_dob").setValue(map.get("app_dob"));
		fields.get("app_sex").setValue(map.get("app_sex"));
		fields.get("app_father").setValue(map.get("app_father"));
		fields.get("app_mother").setValue(map.get("app_mother"));
		fields.get("business").setValue(map.get("business"));
		fields.get("govt_service").setValue(map.get("govt_service"));
		fields.get("pvt_service").setValue(map.get("pvt_service"));
		fields.get("agriculture").setValue(map.get("agriculture"));
		fields.get("others").setValue(map.get("others"));
		fields.get("address1").setValue(map.get("address1"));
		fields.get("address2").setValue(map.get("address2"));
		fields.get("city").setValue(map.get("city"));
		fields.get("state").setValue(map.get("state"));
		fields.get("pin").setValue(map.get("pin"));
		fields.get("pan_no").setValue(map.get("pan_no"));
		fields.get("mob_no").setValue(map.get("mob_no"));
		fields.get("nom_name").setValue(map.get("nom_name"));
		fields.get("nom_age").setValue(map.get("nom_age"));
		fields.get("nom_reln").setValue(map.get("nom_reln"));
		fields.get("nom_add").setValue(map.get("nom_add"));
		fields.get("mem_fee_fig").setValue(map.get("mem_fee_fig"));
		fields.get("mem_fee_words").setValue(map.get("mem_fee_words"));
		fields.get("auth_sign").setValue(map.get("auth_sign"));
		fields.get("app_sign").setValue(map.get("app_sign"));
		fields.get("photocopy_of_pan_card").setValue(map.get("photocopy_of_pan_card"));
		fields.get("pan_card_no").setValue(map.get("pan_card_no"));
		fields.get("photocopy_of_phone_bill").setValue(map.get("photocopy_of_phone_bill"));
		fields.get("mobile_bill_no").setValue(map.get("mobile_bill_no"));
		fields.get("photocopy_of_passport").setValue(map.get("photocopy_of_passport"));
		fields.get("passport_no").setValue(map.get("passport_no"));
		fields.get("photocopy_electionid").setValue(map.get("photocopy_electionid"));
		fields.get("electionid_no").setValue(map.get("electionid_no"));
		fields.get("photocopy_of_bank_acc").setValue(map.get("photocopy_of_bank_acc"));
		fields.get("bank_acc_no").setValue(map.get("bank_acc_no"));
		fields.get("photocopy _of_driving_license").setValue(map.get("photocopy _of_driving_license"));
		fields.get("driving_license_no").setValue(map.get("driving_license_no"));
		fields.get("photocopy_of_ration_card").setValue(map.get("photocopy_of_ration_card"));
		fields.get("ration_card_no").setValue(map.get("ration_card_no"));
		fields.get("photocopy_of_electricity_bill").setValue(map.get("photocopy_of_electricity_bill"));
		fields.get("electricity_bill_no").setValue(map.get("electricity_bill_no"));
		fields.get("photocopy_of_adhar_card").setValue(map.get("photocopy_of_adhar_card"));
		fields.get("adhar_card_no").setValue(map.get("adhar_card_no"));
		fields.get("other_id_no").setValue(map.get("other_id_no"));
		fields.get("mem_sign").setValue(map.get("mem_sign"));
		fields.get("guarntr_det").setValue(map.get("guarntr_det"));
		fields.get("guarntr_sign").setValue(map.get("guarntr_sign"));
		fields.get("empl_sign").setValue(map.get("empl_sign"));
		acroForm.flattenFields();
		pdfDoc.close();
		reader.close();
	}

}
