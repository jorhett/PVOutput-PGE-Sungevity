package com.droidbytes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.droidbytes.beans.BeanMergeUtil;
import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.google.spreadsheet.AccessGoogleSpreadsheet;
import com.droidbytes.util.PVProperties;
import com.droidbytes.webclient.GetPGEData;
import com.droidbytes.webclient.GetSungevityData;

/**
 * @author maggarwal
 *
 */
public class RecordPVOutput {

	public static void main(String[] args) throws Exception {

		Calendar start = Calendar.getInstance();
		//start.setTime(FileUpdate.readLastDate());
		start.setTime(AccessGoogleSpreadsheet.getLastDate(PVProperties.getProperty("spreadSheetName"),
					PVProperties.getProperty("workSheetName"),
					PVProperties.getProperty("authFilePath")));

		Calendar end = Calendar.getInstance();
		end.setTime(new Date());

		try {
			if (!start.after(end)) {
				start.add(Calendar.DATE, 1);
				publishForDate(start, end);
			}
		} catch (Exception e) {
			// write out last date if exception occurs
			System.out.println("Error: " + e.getLocalizedMessage());
			System.out.println("Stopping at date : " + start.getTime());
			//FileUpdate.writeFileBytes(start.getTime());
		} finally {
			String tempFolder = PVProperties.getProperty("tempFolder");
			FileUtils.deleteQuietly(new File(tempFolder));
		}
	}

	/**
	 * @param dateToProcess
	 * @throws Exception
	 */
	public static void publishForDate(Calendar start, Calendar end) throws Exception {

		LinkedHashMap<String, EnergyForDay> allConsumed = GetPGEData.getUsage(start, end);

		allConsumed.forEach((dateStr, netConsumed) -> {
			try {
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd"); 
				Date dateToProcess = dt.parse(dateStr); 
				 
				EnergyForDay produced = GetSungevityData.getData(dateToProcess);
				EnergyForDay consolidated = BeanMergeUtil.mergeData(netConsumed,
						produced);
	
					publishPVOutput(consolidated, PVProperties.getProperty("pvOutputKey"),
							PVProperties.getProperty("pvOutputSystemId"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		

	}

	/**
	 * @param spfd
	 * @param pvOutputKey
	 * @param pvOutputSystemId
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static void publishPVOutput(EnergyForDay spfd, String pvOutputKey,
			String pvOutputSystemId) throws ClientProtocolException,
			IOException {

		String postBody = "data="
				+ spfd.toString().substring(0, spfd.toString().length() - 1);
		System.out.println("--------------------" + new Date()
				+ "----------------------------------------------------");
		System.out.println("Request: " + postBody);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(
				"http://pvoutput.org/service/r2/addbatchstatus.jsp");
		httpPost.addHeader("X-Pvoutput-Apikey", pvOutputKey);
		httpPost.addHeader("X-Pvoutput-SystemId", pvOutputSystemId);
		httpPost.addHeader(HttpHeaders.CONTENT_TYPE,
				"application/x-www-form-urlencoded");

		httpPost.setEntity(new StringEntity(postBody));

		CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			response.append(inputLine);
		}
		reader.close();
		httpClient.close();

		// print result
		System.out.println("-------------");
		System.out.println("status:"
				+ httpResponse.getStatusLine().getStatusCode() + " response: "
				+ response.toString());
		System.out
				.println("------------------------------------------------------------------");
	}

}
