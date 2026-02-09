//package com.kovan.config;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//@Component
//@ConfigurationProperties(prefix = "docassemble")
//public class DocassembleConfig {
//    private ApiConfig api;
//    private InterviewConfig interview;
//
//    public ApiConfig getApi() { return api; }
//    public void setApi(ApiConfig api) { this.api = api; }
//
//    public InterviewConfig getInterview() { return interview; }
//    public void setInterview(InterviewConfig interview) { this.interview = interview; }
//
//    public static class ApiConfig {
//        private String url;
//        private String key;
//
//        public String getUrl() { return url; }
//        public void setUrl(String url) { this.url = url; }
//        public String getKey() { return key; }
//        public void setKey(String key) { this.key = key; }
//    }
//
//    public static class InterviewConfig {
//        private String name;
//        private String packageName;
//
//        public String getName() { return name; }
//        public void setName(String name) { this.name = name; }
//        public String getPackageName() { return packageName; }
//        public void setPackageName(String packageName) { this.packageName = packageName; }
//    }
//}
