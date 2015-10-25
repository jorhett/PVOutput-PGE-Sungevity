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
import com.google.gson.Gson;

public class GetSungevityData  {

	public static EnergyForDay getData(String username, String password, Date dateOfProduction) throws Exception {
		EnergyForDay production = new EnergyForDay();
		Client cb = ClientBuilder.newClient();
		WebTarget baseTarget = cb.target("https://api.sungevity.com/v1");
		LoginResponse login = getLoginToken(baseTarget, username, password);
		if (login == null) {
			login = getLoginToken(baseTarget, username, password);
		}
		String custProfile = getCustomerProfile(baseTarget, login);
		PerformanceData perfData = getPerformanceData(baseTarget, login, custProfile, dateOfProduction);
		for (Iterator<Performance> perfIter = perfData.getProperties().getPerformance().iterator(); perfIter.hasNext();) {
			Performance perf = (Performance) perfIter.next();
			EnergyUnit unit = new EnergyUnit(perf.getDateObj(), perf.getwh()>0?perf.getwh():0, 0);
			production.addSolarProductionUnit(unit);
		}
		return production;

	}
	
	private static PerformanceData getPerformanceData(WebTarget baseTarget,LoginResponse login,
			String custProfile, Date dateToFetchData) {
		
		WebTarget getStuff = baseTarget.path("/installation/");
		getStuff = getStuff.path(custProfile).path("performance");
		getStuff = getStuff.queryParam("granularity", "hourly").queryParam("from", String.format("%1$tF", dateToFetchData));
		Invocation.Builder ib = getStuff.request("application/vnd.siren+json");
		ib.header("Authorization", "Bearer " + login.getAccessToken());
		Response resp = ib.get();

		Gson gson = new Gson();
		PerformanceData performanceData = gson.fromJson(resp.readEntity(String.class),
				PerformanceData.class);
		return performanceData;
	}

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
