package com.fanap.podchat.requestobject;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

public class RequestUploadImage {

    private Activity activity;
    private Uri fileUri;

    RequestUploadImage(Builder builder) {
        this.activity = builder.activity;
        this.fileUri = builder.fileUri;

    }

    public static class Builder {
        private Activity activity;
        private Uri fileUri;

        public Builder(Activity activity, Uri fileUri) {
            this.activity = activity;
            this.fileUri = fileUri;
        }

        @NonNull
        public RequestUploadImage build() {
            return new RequestUploadImage(this);
        }
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }
}
