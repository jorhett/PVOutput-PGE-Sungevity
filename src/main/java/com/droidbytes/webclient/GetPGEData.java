package com.droidbytes.webclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.pge.PGEDataParser;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class GetPGEData {

	public static String folderName = "/tmp/pgeData";
	public static final String pgeElectricUsageFilePrefix = "pge_electric";
	
	public static EnergyForDay getUsage(String username, String password, Date dateOfUse, String tempFolder) throws MalformedURLException, IOException, NumberFormatException, ParseException {

		String zipFile = downloadPGEData(username, password, dateOfUse);
		String electricUsage = parsePgeData(zipFile);
		EnergyForDay production = PGEDataParser.parseFile(electricUsage);
		if (null != tempFolder) {
			folderName = tempFolder;
		}
		FileUtils.deleteQuietly(new File(folderName));
		return production;
		
	}

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
				if (entryName != null && entryName.startsWith(pgeElectricUsageFilePrefix)) {
					electricUsageFile = folderName + File.separator + entryName;
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

	private static String downloadPGEData(String user, String password,
			Date dateToDownload) throws IOException,
			MalformedURLException {
		String fileName = null; 
		String dateToDownloadStr = new SimpleDateFormat("MM/dd/yyyy").format(dateToDownload);
		String monthToDownloadStr  = new SimpleDateFormat("yyyy-M").format(dateToDownload); // "2015-9"
		try (final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11)) {
			Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(
					java.util.logging.Level.OFF);
			Logger.getLogger("org.apache.http").setLevel(
					java.util.logging.Level.OFF);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			// load home page
			HtmlPage page = webClient.getPage("http://www.pge.com/");

			// login
			List<HtmlForm> forms = page.getForms();
			HtmlForm loginForm = forms.get(1);
			HtmlTextInput userName = loginForm.getInputByName("USER");
			HtmlPasswordInput passWord = loginForm.getInputByName("PASSWORD");
			userName.setValueAttribute(user);
			passWord.setValueAttribute(password);
			HtmlSubmitInput button = page
					.getFirstByXPath("//*[@id=\"login-btn\"]");
			HtmlPage page2 = button.click();

			// click on my usage
			HtmlAnchor myUsageLink = page2.getAnchorByText("My Usage");
			page2 = myUsageLink.click();

			// click on green button
			myUsageLink = page2
					.getAnchorByText("Green Button - Download my data");
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

			page2 = myUsageLink.click();

			File directory = new File(folderName);

			// if the output directory doesn't exist, create it
			if (!directory.exists())
				directory.mkdirs();

			// download zip file
			java.net.URL url = new java.net.URL(
					"https://pge.opower.com/ei/app/modules/customer/"
							+ customerNumber + "/energy/download");
			WebRequest requestSettings = new WebRequest(url, HttpMethod.GET);
			requestSettings
					.setRequestParameters(new ArrayList<NameValuePair>());
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
			UnexpectedPage page3 = webClient.getPage(requestSettings);
			InputStream is = page3.getWebResponse().getContentAsStream();
			fileName = folderName + File.separator
					+ String.format("%1$tY%1$tm%1$td-%1$tR", new Date())
					+ ".zip";
			Path zipFilePath = Paths.get(fileName);
			Files.copy(is, zipFilePath);
			webClient.closeAllWindows();
		}
		return fileName;
	}

}
