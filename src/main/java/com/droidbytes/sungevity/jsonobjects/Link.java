
package com.droidbytes.sungevity.jsonobjects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Link {

    @SerializedName("rel")
    @Expose
    private List<String> rel = new ArrayList<String>();
    @SerializedName("href")
    @Expose
    private String href;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Link() {
    }

    /**
     * 
     * @param rel
     * @param href
     */
    public Link(List<String> rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    /**
     * 
     * @return
     *     The rel
     */
    public List<String> getRel() {
        return rel;
    }

    /**
     * 
     * @param rel
     *     The rel
     */
    public void setRel(List<String> rel) {
        this.rel = rel;
    }

    public Link withRel(List<String> rel) {
        this.rel = rel;
        return this;
    }

    /**
     * 
     * @return
     *     The href
     */
    public String getHref() {
        return href;
    }

    /**
     * 
     * @param href
     *     The href
     */
    public void setHref(String href) {
        this.href = href;
    }

    public Link withHref(String href) {
        this.href = href;
        return this;
    }

}
