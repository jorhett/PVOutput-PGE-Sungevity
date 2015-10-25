
package com.droidbytes.sungevity.jsonobjects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Properties {

    @SerializedName("performance")
    @Expose
    private List<Performance> performance = new ArrayList<Performance>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Properties() {
    }

    /**
     * 
     * @param performance
     */
    public Properties(List<Performance> performance) {
        this.performance = performance;
    }

    /**
     * 
     * @return
     *     The performance
     */
    public List<Performance> getPerformance() {
        return performance;
    }

    /**
     * 
     * @param performance
     *     The performance
     */
    public void setPerformance(List<Performance> performance) {
        this.performance = performance;
    }

    public Properties withPerformance(List<Performance> performance) {
        this.performance = performance;
        return this;
    }

}
