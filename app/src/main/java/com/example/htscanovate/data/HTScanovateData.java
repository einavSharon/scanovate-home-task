package com.example.htscanovate.data;

import android.os.Parcel;
import android.os.Parcelable;

public class HTScanovateData implements Parcelable {

    private String jsonResult;

    protected HTScanovateData(Parcel in) {
        jsonResult = in.readString();
    }

    public static final Creator<HTScanovateData> CREATOR = new Creator<HTScanovateData>() {
        @Override
        public HTScanovateData createFromParcel(Parcel in) {
            return new HTScanovateData(in);
        }

        @Override
        public HTScanovateData[] newArray(int size) {
            return new HTScanovateData[size];
        }
    };

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jsonResult);
    }
}
