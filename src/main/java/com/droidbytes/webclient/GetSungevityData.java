package com.droidbytes.webclient;

import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.beans.EnergyUnit;
import com.droidbytes.sungevity.jsonobjects.LoginResponse;
import com.droidbytes.sungevity.jsonobjects.Performance;
import com.droidbytes.sungevity.jsonobjects.PerformanceData;
import com.droidbytes.util.PVProperties;
import com.google.gson.Gson;

/**
 * 
 * Get data from Sungevity
 * @author maggarwal
 *
 */
public class GetSungevityData  {

	private static WebTarget baseTarget = null;
	private static LoginResponse loginResponse = null;
	private static String custProfile = null;
	
	/**
	 * Initalize class and do initial login
	 * @throws Exception
	 */
	private static void init() throws Exception {
		Client cb = ClientBuilder.newClient();
		baseTarget = cb.target("https://api.sungevity.com/v1");
		loginResponse = getLoginToken(baseTarget, PVProperties.getProperty("sungevityUsername"),
				PVProperties.getProperty("sungevityPassword"));
		if (loginResponse == null) {
			loginResponse = getLoginToken(baseTarget,PVProperties.getProperty("sungevityUsername"),
					PVProperties.getProperty("sungevityPassword"));
		}
		custProfile = getCustomerProfile(baseTarget, loginResponse);
	}
	
	/**
	 * main method exposed
	 * @param dateOfProduction
	 * @return
	 * @throws Exception
	 */
	public static EnergyForDay getData(Date dateOfProduction) throws Exception {
		
		if (baseTarget == null) {
			init();
		}
		
		EnergyForDay production = new EnergyForDay();

		PerformanceData perfData = getPerformanceData(dateOfProduction);
		for (Iterator<Performance> perfIter = perfData.getProperties().getPerformance().iterator(); perfIter.hasNext();) {
			Performance perf = (Performance) perfIter.next();
			EnergyUnit unit = new EnergyUnit(perf.getDateObj(), perf.getwh()>0?perf.getwh():0, 0);
			production.addSolarProductionUnit(unit);
		}
		return production;

	}
	
	/**
	 * download performance data. reusable for multiple dates
	 * @param baseTarget
	 * @param loginResponse
	 * @param custProfile
	 * @param dateToFetchData
	 * @return
	 */
	private static PerformanceData getPerformanceData(Date dateToFetchData) {
		
		WebTarget getStuff = baseTarget.path("/installation/");
		getStuff = getStuff.path(custProfile).path("performance");
		getStuff = getStuff.queryParam("granularity", "hourly").queryParam("from", String.format("%1$tF", dateToFetchData));
		Invocation.Builder ib = getStuff.request("application/vnd.siren+json");
		ib.header("Authorization", "Bearer " + loginResponse.getAccessToken());
		Response resp = ib.get();

		Gson gson = new Gson();
		PerformanceData performanceData = gson.fromJson(resp.readEntity(String.class),
				PerformanceData.class);
		return performanceData;
	}

	/**
	 * getting login token
	 * @param baseTarget
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static LoginResponse getLoginToken(WebTarget baseTarget, String username, String password) throws Exception {
		WebTarget getStuff = baseTarget.path("/token");
		Form form = new Form();
		form.param("grant_type", "password");
		form.param("username", username);
		form.param("password", password);
		form.param("client_id", "1");

		Invocation.Builder ib = getStuff.request("application/vnd.siren+json");
		Response resp = ib.post(Entity.entity(form,
				MediaType.APPLICATION_FORM_URLENCODED_TYPE));

		Gson gson = new Gson();
		LoginResponse loginResponse = gson.fromJson(resp.readEntity(String.class),
				LoginResponse.class);
		return loginResponse;

	}
	
	/**
	 * Get customer profile string
	 * @param baseTarget
	 * @param login
	 * @return
	 * @throws Exception
	 */
	public static String getCustomerProfile(WebTarget baseTarget, LoginResponse login) throws Exception {
		WebTarget getStuff = baseTarget.path("/current-user");
		Invocation.Builder ib = getStuff.request("application/vnd.siren+json");
		ib.header("Authorization", "Bearer " + login.getAccessToken());
		Response resp = ib.get();
		String customerId = null;
		String customerProfile = resp.readEntity(String.class);
		Pattern pattern4=Pattern.compile("\"accountId\":\"(.*?)\",");
		Matcher matcher = pattern4.matcher(customerProfile);
		if (matcher.find()) {
			customerId = matcher.group(1);
		}
		
		return customerId;
		
		
	}

}
