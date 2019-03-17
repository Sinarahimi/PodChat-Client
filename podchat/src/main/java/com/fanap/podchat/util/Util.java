package com.fanap.podchat.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fanap.podchat.mainmodel.Contact;
import com.fanap.podchat.mainmodel.MessageVO;
import com.fanap.podchat.model.ChatResponse;
import com.fanap.podchat.model.Contacts;
import com.fanap.podchat.model.ResultAddContact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Util {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @NonNull
    public static ChatResponse<ResultAddContact> getReformatOutPutAddContact(Contacts contacts, String uniqueId) {
        ChatResponse<ResultAddContact> chatResponse = new ChatResponse<>();
        chatResponse.setUniqueId(uniqueId);

        ResultAddContact resultAddContact = new ResultAddContact();
        resultAddContact.setContentCount(1);
        Contact contact = new Contact();

        contact.setCellphoneNumber(contacts.getResult().get(0).getCellphoneNumber());
        contact.setEmail(contacts.getResult().get(0).getEmail());
        contact.setFirstName(contacts.getResult().get(0).getFirstName());
        contact.setId(contacts.getResult().get(0).getId());
        contact.setLastName(contacts.getResult().get(0).getLastName());
        contact.setUniqueId(contacts.getResult().get(0).getUniqueId());
        resultAddContact.setContact(contact);
        chatResponse.setResult(resultAddContact);
        return chatResponse;
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    public static <T extends Number> boolean isNullOrEmpty(@Nullable ArrayList<T> list) {
        return list == null || list.size() == 0;
    }

    public static <T extends Number> boolean isNullOrEmpty(@Nullable T number) {
        String num = String.valueOf(number);
        return number == null || num.equals("0");
    }

    public static <T extends Object> boolean isNullOrEmpty(@Nullable List<T> list) {
        return list == null || list.size() == 0;
    }

    public static boolean isNullOrEmptyMessageVO(@Nullable List<MessageVO> list) {
        return list == null || list.size() == 0;
    }

    public static <T extends Number> boolean isNullOrEmptyNumber(@Nullable List<T> list) {
        return list == null || list.size() == 0;
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static <T> String listToJson(ArrayList<T> list, Gson gson) {
        Type listType = new TypeToken<List<T>>() {
        }.getType();

        return gson.toJson(list, listType);
    }

    public static <T> List<T> JsonToList(String json,Gson gson){

      return  gson.fromJson(json, new TypeToken<List<T>>() {
        }.getType());
    }
}
