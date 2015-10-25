
package com.droidbytes.sungevity.jsonobjects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Performance {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2015-09-25 00:00:00
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("kwh")
    @Expose
    private Double kwh;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Performance() {
    }

    /**
     * 
     * @param kwh
     * @param date
     */
    public Performance(String date, Double kwh) {
        this.date = date;
        this.kwh = kwh;
    }

    /**
     * 
     * @return
     *     The date
     */
    public String getDate() {
        return date;
    }

    public Date getDateObj() throws ParseException {
    	return sdf.parse(date);
    }
    
    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    public Performance withDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * 
     * @return
     *     The kwh
     */
    public Double getKwh() {
        return kwh;
    }

    public long getwh() {
        return Math.round(kwh * 1000);
    }
    
    /**
     * 
     * @param kwh
     *     The kwh
     */
    public void setKwh(Double kwh) {
        this.kwh = kwh;
    }

    public Performance withKwh(Double kwh) {
        this.kwh = kwh;
        return this;
    }

}
