package com.droidbytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.droidbytes.beans.BeanMergeUtil;
import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.util.FileUpdate;
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
		start.setTime(FileUpdate.readLastDate());

		Calendar end = Calendar.getInstance();
		end.setTime(new Date());

		try {
			while( !start.after(end)){
			    Date targetDay = start.getTime();
			    System.out.println("Processing for date : " + targetDay);
			    publishForDate(targetDay);
			    start.add(Calendar.DATE, 1);
			}
		} catch (Exception e) {
			// write out last date if exception occurs
			System.out.println("Stopping at date : " + start.getTime());
			FileUpdate.writeFileBytes(start.getTime());
		}
	}

	/**
	 * @param dateToProcess
	 * @throws Exception
	 */
	public static void publishForDate(Date dateToProcess) throws Exception {
		
		EnergyForDay netConsumed = GetPGEData.getUsage(dateToProcess);
	
		EnergyForDay produced = GetSungevityData.getData(dateToProcess);

		EnergyForDay consolidated = BeanMergeUtil.mergeData(netConsumed,
				produced);
		publishPVOutput(consolidated, PVProperties.getProperty("pvOutputKey"),
				PVProperties.getProperty("pvOutputSystemId"));

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
