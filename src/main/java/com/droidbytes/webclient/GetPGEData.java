package com.droidbytes.webclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.google.spreadsheet.AccessGoogleSpreadsheet;
import com.droidbytes.pge.PGEDataParser;
import com.droidbytes.util.PVProperties;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * @author maggarwal
 *
 */
public class GetPGEData {

	public static String Folder_Name = "/tmp/pgeData";
	public static final String Pge_Electric_Usage_File_Prefix = "pge_electric";
	private static WebClient Web_Client = null;
	private static URL File_Download_Url = null;
	private static HtmlPage downloadPage = null;

	/**
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private static void init() throws IOException, InterruptedException {

		String user = PVProperties.getProperty("pgeUserName");
		String password = PVProperties.getProperty("pgePassword");

		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(
				java.util.logging.Level.OFF);
		Logger.getLogger("org.apache.http").setLevel(
				java.util.logging.Level.OFF);

		//Web_Client = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
		Web_Client = new WebClient(BrowserVersion.CHROME);
		Web_Client.getOptions().setCssEnabled(false);
		Web_Client.getOptions().setThrowExceptionOnScriptError(false);
		Web_Client.setJavaScriptTimeout(15000);
		//Web_Client.setAjaxController(new NicelyResynchronizingAjaxController());
		// load home page
		HtmlPage page = Web_Client.getPage("http://www.pge.com/");

		// login
		HtmlForm loginForm = page.getFormByName("login-form");
		HtmlTextInput userName = loginForm.getInputByName("username");
		HtmlPasswordInput passWord = loginForm.getInputByName("password");
		userName.setValueAttribute(user);
		passWord.setValueAttribute(password);
		HtmlButton button = loginForm.getButtonByName("btnLogin");
		
		WebWindow window = page.getEnclosingWindow();

		button.click();

		int PAGE_RETRY = 100; 
		for (int i = 0; !page.asXml().contains("utag-usage-and-ways-to-save-my-usage") && i < PAGE_RETRY; i++) {
	        try {
	            Thread.sleep(500);
	            page = (HtmlPage)window.getEnclosedPage();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		
		//click on my usage
		HtmlAnchor myUsageLink = page.getAnchorByHref("https://www.pge.com/myenergyweb/appmanager/pge/customer/contextual/myusage");
		downloadPage = myUsageLink.click();

	}

	/**
	 * @param dateOfUse
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws ParseException
	 * @throws InterruptedException 
	 */
	public static EnergyForDay getUsage(Date dateOfUse)
			throws MalformedURLException, IOException, NumberFormatException,
			ParseException, InterruptedException {

		String zipFile = downloadPGEData(dateOfUse);
		String electricUsage = parsePgeData(zipFile);
		EnergyForDay production = PGEDataParser.parseFile(electricUsage);
		
		// recrod in worksheet
		try {
			long netDeltaForDay = production.totalConsumptionForDay();
			AccessGoogleSpreadsheet.writeEntry(dateOfUse, netDeltaForDay,
					PVProperties.getProperty("spreadSheetName"),
					PVProperties.getProperty("workSheetName"),
					PVProperties.getProperty("authFilePath"));
			
		} catch (Exception e) {
			System.out.println("Error writing data to google spreadhseet. " + e.getLocalizedMessage());
		}

		return production;

	}

	/**
	 * @param zipFilePath
	 * @return
	 * @throws IOException
	 */
	private static String parsePgeData(String zipFilePath) throws IOException {

		String electricUsageFile = null;
		// buffer for read and write data to file
		byte[] buffer = new byte[2048];

		try {
			FileInputStream fInput = new FileInputStream(zipFilePath);
			ZipInputStream zipInput = new ZipInputStream(fInput);

			ZipEntry entry = zipInput.getNextEntry();

			while (entry != null) {
				String entryName = entry.getName();
				if (entryName != null
						&& entryName.startsWith(Pge_Electric_Usage_File_Prefix)) {
					electricUsageFile = Folder_Name + File.separator + entryName;
					File file = new File(electricUsageFile);

					// create the directories of the zip directory
					if (entry.isDirectory()) {
						File newDir = new File(file.getAbsolutePath());
						if (!newDir.exists()) {
							boolean success = newDir.mkdirs();
							if (success == false) {
								System.out.println("Problem creating Folder");
							}
						}
					} else {
						FileOutputStream fOutput = new FileOutputStream(file);
						int count = 0;
						while ((count = zipInput.read(buffer)) > 0) {
							// write 'count' bytes to the file output stream
							fOutput.write(buffer, 0, count);
						}
						fOutput.close();
					}
					// close ZipEntry and take the next one
					zipInput.closeEntry();
					break;
				}
				entry = zipInput.getNextEntry();
			}

			// close the last ZipEntry
			zipInput.closeEntry();
			zipInput.close();
			fInput.close();

			return electricUsageFile;
		} catch (IOException e) {
			throw e;
		}

	}

	/**
	 * @param dateToDownload
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws InterruptedException 
	 */
	private static String downloadPGEData(Date dateToDownload)
			throws IOException, MalformedURLException, InterruptedException {

		if (Web_Client == null) {
			init();
		}

		// click on green button
		
		int PAGE_RETRY = 100;
		for (int i = 0; !downloadPage.asXml().contains("Green Button - Download my data") && i < PAGE_RETRY; i++) {
	        try {
	            Thread.sleep(500);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		
		HtmlAnchor myUsageLink = downloadPage.getAnchorByText("Green Button - Download my data");
		String myDownloadLink = myUsageLink.getHrefAttribute();

		Pattern pattern = Pattern.compile("/customer/(\\d+)/bill_periods");
		String customerNumber = null;
		Matcher matcher = pattern.matcher(myDownloadLink);
		if (matcher.find()) {
			customerNumber = matcher.group(1);
		} else {
			throw new IOException("Customer number not found in URL: "
					+ myDownloadLink);
		}

		HtmlPage page2 = myUsageLink.click();

		File directory = new File(Folder_Name);

		// if the output directory doesn't exist, create it
		if (!directory.exists())
			directory.mkdirs();

		// download zip file
		File_Download_Url = new URL(
				"https://pge.opower.com/ei/app/modules/customer/"
						+ customerNumber + "/energy/download");

		String fileName = null;
		String dateToDownloadStr = new SimpleDateFormat("MM/dd/yyyy")
				.format(dateToDownload);
		String monthToDownloadStr = new SimpleDateFormat("yyyy-M")
				.format(dateToDownload); // "2015-9"

		WebRequest requestSettings = new WebRequest(File_Download_Url,
				HttpMethod.GET);
		requestSettings.setRequestParameters(new ArrayList<NameValuePair>());
		requestSettings.getRequestParameters().add(
				new NameValuePair("bill", monthToDownloadStr));
		requestSettings.getRequestParameters().add(
				new NameValuePair("exportFormat", "CSV_AMI"));
		requestSettings.getRequestParameters().add(
				new NameValuePair("csvFrom", dateToDownloadStr));
		requestSettings.getRequestParameters().add(
				new NameValuePair("csvTo", dateToDownloadStr));
		requestSettings.getRequestParameters().add(
				new NameValuePair("xmlFrom", dateToDownloadStr));
		requestSettings.getRequestParameters().add(
				new NameValuePair("xmlTo", dateToDownloadStr));

		// filename
		UnexpectedPage downloadPage = Web_Client.getPage(requestSettings);
		InputStream is = downloadPage.getWebResponse().getContentAsStream();
		fileName = Folder_Name + File.separator
				+ String.format("%1$tY%1$tm%1$td-%1$tR", dateToDownload) + ".zip";
		Path zipFilePath = Paths.get(fileName);
		Files.copy(is, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
		return fileName;
	}

}
