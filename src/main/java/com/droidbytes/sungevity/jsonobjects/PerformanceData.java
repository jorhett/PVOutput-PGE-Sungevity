
package com.droidbytes.sungevity.jsonobjects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class PerformanceData {

    @SerializedName("class")
    @Expose
    private List<String> _class = new ArrayList<String>();
    @SerializedName("properties")
    @Expose
    private Properties properties;
    @SerializedName("links")
    @Expose
    private List<Link> links = new ArrayList<Link>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public PerformanceData() {
    }

    /**
     * 
     * @param _class
     * @param links
     * @param properties
     */
    public PerformanceData(List<String> _class, Properties properties, List<Link> links) {
        this._class = _class;
        this.properties = properties;
        this.links = links;
    }

    /**
     * 
     * @return
     *     The _class
     */
    public List<String> getClass_() {
        return _class;
    }

    /**
     * 
     * @param _class
     *     The class
     */
    public void setClass_(List<String> _class) {
        this._class = _class;
    }

    public PerformanceData withClass(List<String> _class) {
        this._class = _class;
        return this;
    }

    /**
     * 
     * @return
     *     The properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 
     * @param properties
     *     The properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public PerformanceData withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 
     * @return
     *     The links
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * 
     * @param links
     *     The links
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public PerformanceData withLinks(List<Link> links) {
        this.links = links;
        return this;
    }

}
