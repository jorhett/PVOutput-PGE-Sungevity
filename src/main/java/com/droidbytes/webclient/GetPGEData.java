package com.droidbytes.webclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.google.spreadsheet.AccessGoogleSpreadsheet;
import com.droidbytes.pge.PGEDataParser;
import com.droidbytes.util.PVProperties;

/**
 * @author maggarwal
 *
 */
public class GetPGEData {

	public static String Folder_Name = "/tmp/pgeData";
	public static final String Pge_Electric_Usage_File_Prefix = "pge_electric";

	/**
	 * @param dateOfUse
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws ParseException
	 * @throws InterruptedException
	 */
	public static LinkedHashMap<String, EnergyForDay> getUsage(Calendar start, Calendar end)
			throws MalformedURLException, IOException, NumberFormatException, ParseException, InterruptedException {

		String zipFile = downloadPGEData(start, end);
		String unzippedElectricUsage = unzipPgeData(zipFile);
		LinkedHashMap<String, EnergyForDay> productionMap = PGEDataParser.parseFile(unzippedElectricUsage);

		productionMap.forEach((date, production) -> {
			try {
				long netDeltaForDay = production.totalConsumptionForDay();
				Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);

				AccessGoogleSpreadsheet.writeEntry(parsedDate, netDeltaForDay,
						PVProperties.getProperty("spreadSheetName"), PVProperties.getProperty("workSheetName"),
						PVProperties.getProperty("authFilePath"));
			} catch (Exception e) {
				System.out.println("Error writing data to google spreadhseet. " + e.getLocalizedMessage());
			}

		});

		return productionMap;

	}

	/**
	 * @param zipFilePath
	 * @return
	 * @throws IOException
	 */
	private static String unzipPgeData(String zipFilePath) throws IOException {

		String electricUsageFile = null;
		// buffer for read and write data to file
		byte[] buffer = new byte[2048];

		try {
			FileInputStream fInput = new FileInputStream(zipFilePath);
			ZipInputStream zipInput = new ZipInputStream(fInput);

			ZipEntry entry = zipInput.getNextEntry();

			while (entry != null) {
				String entryName = entry.getName();
				if (entryName != null && entryName.startsWith(Pge_Electric_Usage_File_Prefix)) {
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
	private static String downloadPGEData(Calendar start, Calendar end) throws IOException, InterruptedException {

		File directory = new File(Folder_Name);

		// if the output directory doesn't exist, create it
		if (!directory.exists())
			directory.mkdirs();

		String user = PVProperties.getProperty("pgeUserName");
		String password = PVProperties.getProperty("pgePassword");
		String startdateStr = new SimpleDateFormat("yyyy-MM-dd").format(start.getTime());
		String enddateStr = new SimpleDateFormat("yyyy-MM-dd").format(end.getTime());
		String fileName = Folder_Name + File.separator + String.format("%1$tY%1$tm%1$td-%1$tR", start) + ".zip";

		ProcessBuilder pb = new ProcessBuilder(
				PVProperties.getProperty("casperRunner"), PVProperties.getProperty("pgeScript"),
				startdateStr, enddateStr, fileName, user, password).inheritIO();

		 Process p = pb.start();
		 int errCode = p.waitFor();
		 System.out.println("downloaded file with status code = " + errCode);
		 return fileName;
	}

}
