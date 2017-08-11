package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AddonInfo {
    //gfgg
    //{"key":"com.thed.zephyr.je","version":"1.0.6-AC","state":"ENABLED","host":{"product":"JIRA","contacts":[{"product":"David Bakes","email":"dbakes@thinkprocurement.com"}]},"license":{"active":true,"type":"COMMERCIAL","evaluation":false,"supportEntitlementNumber":"SEN-2469139"},"links":{"marketplace":[{"href":"https:\/\/marketplace.atlassian.com\/plugins\/com.thed.zephyr.je"}],"self":[{"href":"https:\/\/thinkprocurement.jira.com\/rest\/atlassian-connect\/1\/addons\/com.thed.zephyr.je"}]}}
    private String key;
    private String version;
    private String state;
    private Host host;
    private License license;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    @JsonIgnoreProperties
    public static class License{
        private Boolean active;
        private String type;
        private Boolean evaluation;
        private String supportEntitlementNumber;

        public Boolean isActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean isEvaluation() {
            return evaluation;
        }

        public void setEvaluation(Boolean evaluation) {
            this.evaluation = evaluation;
        }

        public String getSupportEntitlementNumber() {
            return supportEntitlementNumber;
        }

        public void setSupportEntitlementNumber(String supportEntitlementNumber) {
            this.supportEntitlementNumber = supportEntitlementNumber;
        }
    }

    @JsonIgnoreProperties
    public static class Host{
        private String product;
        private List<Contact> contacts;

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }
    }

    @JsonIgnoreProperties
    public static class Contact{
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
