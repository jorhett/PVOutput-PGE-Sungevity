package com.droidbytes.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FileUpdate {

	private static final String waterMarkFileName = "PVLastProcessedDate.txt";
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static void main(String args[]) throws ParseException {
		writeFileBytes(new Date());
		System.out.println(readLastDate());
	}
	
	public static void writeFileBytes(Date dateToWrite) {
		try {
			String strDateToWrite = dateFormatter.format(dateToWrite);

			try {
				File dateFile = new File(getFilePath().toString());
				dateFile.getParentFile().mkdirs();
				dateFile.createNewFile();
			} catch (FileAlreadyExistsException ignored) {
				// ignore if file already exist
			}

			Files.write(getFilePath(), strDateToWrite.getBytes());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static Path getFilePath() {
		return Paths.get(PVProperties.getProperty("tempFolder")
				+ File.separator + waterMarkFileName);
	}

	public static Date readLastDate() throws ParseException {
		Date dateRead;
		try {
			List<String> lines = Files.readAllLines(getFilePath(),
					StandardCharsets.UTF_8);
			dateRead = dateFormatter.parse(lines.get(0));
		} catch (Exception e) {
			// could not read the date so go back number of days specified in
			// prop file
			int daysToGoBack = Integer.parseInt(PVProperties
					.getProperty("numberofDaysToGoBack"));
			Date dateToGetData = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateToGetData);
			cal.add(Calendar.DATE, (daysToGoBack * -1));
			dateRead = cal.getTime();
		}
		return dateRead;
	}

}
