package com.droidbytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.droidbytes.beans.BeanMergeUtil;
import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.webclient.GetPGEData;
import com.droidbytes.webclient.GetSungevityData;

public class RecordPVOutput {

	private static Properties configProp = new Properties();
	
	public static void main(String[] args) throws Exception {

		loadProps();
		int daysToGoBack = Integer.parseInt(configProp.getProperty("numberofDaysToGoBack"));
		Date dateToGetData = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateToGetData);
		cal.add(Calendar.DATE, (daysToGoBack * -1));
		Date newDate = cal.getTime();
		EnergyForDay netConsumed = GetPGEData.getUsage(configProp.getProperty("pgeUserName"), configProp.getProperty("pgePassword"), newDate, configProp.getProperty("tempFolder"));
		EnergyForDay produced = GetSungevityData.getData(configProp.getProperty("sungevityUsername"), configProp.getProperty("sungevityPassword"), newDate);
		
		EnergyForDay consolidated = BeanMergeUtil.mergeData(netConsumed, produced);
		publishPVOutput(consolidated, configProp.getProperty("pvOutputKey"), configProp.getProperty("pvOutputSystemId"));
	}
	
	public static void loadProps() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream in = classloader.getResourceAsStream("config.properties");
        try {
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
}
	
	public static void publishPVOutput(EnergyForDay spfd, String pvOutputKey, String pvOutputSystemId) throws ClientProtocolException, IOException {

        String postBody = "data=" + spfd.toString().substring(0,spfd.toString().length()-1);
        System.out.println("--------------------" + new Date() + "----------------------------------------------------");
        System.out.println("Request: " + postBody);
		CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://pvoutput.org/service/r2/addbatchstatus.jsp");
        httpPost.addHeader("X-Pvoutput-Apikey", pvOutputKey);
        httpPost.addHeader("X-Pvoutput-SystemId", pvOutputSystemId);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        
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
        System.out.println("status:" + httpResponse.getStatusLine().getStatusCode() + " response: " + response.toString());
        System.out.println("------------------------------------------------------------------");
	}

}
