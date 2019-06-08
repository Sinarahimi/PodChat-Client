package com.fanap.podchat.requestobject;

import java.util.ArrayList;

public class RequestSeenDuration {

    private ArrayList<Long> userIds;

    RequestSeenDuration(Builder builder){
        this.userIds = builder.userIds;
    }


    public static class Builder{
        private ArrayList<Long> userIds;

        public Builder userIds(ArrayList<Long> userIds){
            this.userIds = userIds;
            return this;
        }

        public RequestSeenDuration build(){
            return new RequestSeenDuration(this);
        }
    }

    public ArrayList<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(ArrayList<Long> userIds) {
        this.userIds = userIds;
    }
}
