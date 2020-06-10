package com.example.htscanovate.network.body;

import android.util.Xml;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;


public class HTScanovateHttpBody extends HttpBody {

    @Override
    public void writeToStream(OutputStream outputStream) {
        try {

            byte[] buffer = getContentAsBytes();

            outputStream.write(buffer);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getContentAsString() {

        StringBuilder bodyFormString = new StringBuilder();
        Set<String> keySet = bodyParams.keySet();
        for (String key : keySet) {
            String value = bodyParams.get(key);
            try {
                value = URLEncoder.encode(value, Xml.Encoding.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            bodyFormString.append(String.format("%s=%s&", key, value));
        }

        return bodyFormString.toString();
    }

    @Override
    public byte[] getContentAsBytes() {

        String bodyFormString = getContentAsString();

        byte[] buffer = null;
        try {
            buffer = bodyFormString.toString().getBytes(Xml.Encoding.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}