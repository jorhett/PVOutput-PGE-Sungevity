package com.droidbytes.google.spreadsheet;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

public class AccessGoogleSpreadsheet {

	public static final DateFormat datePart = new SimpleDateFormat("MM/dd/yyyy");
	public static final DateFormat timePart = new SimpleDateFormat("HH:mm:ss");

	public static void main(String[] args) throws Exception {

		String param = null;
		if (args.length > 0) {
			param = args[0];
		}
//		LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
//		// Put elements to the map
//		lhm.put("Date", "4/17/2016");
//		lhm.put("Time", "12:30pm");
//		lhm.put("Delta", "12");
//		lhm.put("Reading", "120");
		//writeDataToSpreadsheet(lhm, "Electric Meter", "Readings", param);
		getLastDate("Electric Meter", "Readings", param);
		
	}

	public static void writeEntry(Date dateTime, long delta,
			String spreadSheetName, String worksheetName, String authString) throws Exception {
		
		LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
		lhm.put("Date", datePart.format(dateTime));
		lhm.put("Time", timePart.format(dateTime));
		lhm.put("Delta", delta + "");
		writeDataToSpreadsheet(lhm, spreadSheetName, worksheetName, authString);

	}
	
	public static Date getLastDate(String spreadSheetName, String worksheetName, String authString) throws Exception {
		SpreadsheetService service = getService(authString);

		WorksheetEntry worksheet = getWorksheetEntry(service, spreadSheetName, worksheetName);
		URL cellFeedUrl = new URI(worksheet.getCellFeedUrl().toString()
		        + "?min-row=2&min-col=1&max-col=1").toURL();
	    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
	    
	    List<CellEntry> cellFeedEntries = cellFeed.getEntries();
	    CellEntry cellEntry = cellFeedEntries.get(cellFeedEntries.size()-1);
	    return datePart.parse(cellEntry.getCell().getInputValue());
		
	}
	

	private static void writeDataToSpreadsheet(LinkedHashMap data,
			String spreadSheetName, String worksheetName, String authString)
			throws Exception {
		ListEntry row = getListEntryRow(data);
		SpreadsheetService service = getService(authString);
		URL listFeedUrl = getListFeedURL(service, spreadSheetName,
				worksheetName);
		service.insert(listFeedUrl, row);
	}

	private static ListEntry getListEntryRow(LinkedHashMap data) {
		ListEntry row = new ListEntry();

		// Get a set of the entries
		Set set = data.entrySet();
		// Get an iterator
		Iterator i = set.iterator();
		// Display elements
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			row.getCustomElements().setValueLocal((String) me.getKey(),
					(String) me.getValue());
		}

		return row;
	}

	private static SpreadsheetService getService(String authString)
			throws IOException, GeneralSecurityException {
		SpreadsheetService service = new SpreadsheetService("ElectricData");
		service.setOAuth2Credentials(authorize(authString));
		return service;
	}

	private static URL getListFeedURL(SpreadsheetService service,
			String spreadSheetName, String worksheetName) throws Exception {
		WorksheetEntry worksheet = getWorksheetEntry( service,
				 spreadSheetName,  worksheetName);
		URL listFeedUrl = worksheet.getListFeedUrl();
		return listFeedUrl;
	}
	
	private static WorksheetEntry getWorksheetEntry(SpreadsheetService service,
			String spreadSheetName, String worksheetName) throws Exception {
		// Define the URL to request. This should never change.
		URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		// Make a request to the API and get all spreadsheets.
		SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
				SpreadsheetFeed.class);

		SpreadsheetEntry sparksheet = getSparkSpreadSheet(feed.getEntries(),
				spreadSheetName);
		WorksheetEntry worksheet = getWorkSheet(sparksheet, service,
				worksheetName);

		return worksheet;
		
	}

	private static SpreadsheetEntry getSparkSpreadSheet(
			List<SpreadsheetEntry> spreadsheets, String spreadSheetName) {
		// Iterate through all of the spreadsheets returned
		for (SpreadsheetEntry spreadsheet : spreadsheets) {
			// Print the title of this spreadsheet to the screen
			if (spreadSheetName.equalsIgnoreCase(spreadsheet.getTitle()
					.getPlainText())) {
				return spreadsheet;
			}
		}
		return null;
	}

	private static WorksheetEntry getWorkSheet(SpreadsheetEntry spreadsheet,
			SpreadsheetService service, String workSheetName) throws Exception {
		try {

			if (spreadsheet != null) {
				WorksheetFeed worksheetFeed = service.getFeed(
						spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
				List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

				for (WorksheetEntry worksheetEntry : worksheets) {
					String wktName = worksheetEntry.getTitle().getPlainText();
					if (wktName.equals(workSheetName)) {
						return worksheetEntry;
					}
				}
			}
		} catch (Exception ex) {
			throw ex;
		}

		return null;
	}

	public static Credential authorize(String filePath) throws IOException,
			GeneralSecurityException {

		String[] SCOPESArray = {
				"https://www.googleapis.com/auth/drive.file",
				"https://spreadsheets.google.com/feeds",
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full",
				"https://docs.google.com/feeds" };
		final List SCOPES = Arrays.asList(SCOPESArray);

		String emailAddress = "1055092297671-ohr14sguhc34a50be6v8qoe0i9g23r79@developer.gserviceaccount.com";
		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
		HttpTransport httpTransport = GoogleNetHttpTransport
				.newTrustedTransport();
		GoogleCredential credential = new GoogleCredential.Builder()
				.setTransport(httpTransport)
				.setJsonFactory(JSON_FACTORY)
				.setServiceAccountId(emailAddress)
				.setServiceAccountPrivateKeyFromP12File(
						new java.io.File(filePath))
				.setServiceAccountScopes(SCOPES).build();

		return credential;
	}

}