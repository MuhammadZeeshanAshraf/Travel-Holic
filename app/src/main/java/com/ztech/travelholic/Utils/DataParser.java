package com.ztech.travelholic.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    private HashMap<String, String> getSingleNearbyPlace(JSONObject googlePlaceJSON)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String nameofPlace = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try
        {
            if(!googlePlaceJSON.isNull("name"))
            {
                nameofPlace = googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("vicinity"))
            {
                vicinity = googlePlaceJSON.getString("vicinity");
            }
            latitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference  = googlePlaceJSON.getString("reference");

            googlePlaceMap.put("place_name", nameofPlace);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);



        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return googlePlaceMap;

    }

    private List<HashMap<String, String>> getAllNearbyPlace(JSONArray jsonArray )
    {
        int counter = 0;
        try {
            counter = jsonArray.length();
        }catch (Exception e)
        {
            // String msg = e.getMessage().toString();
            e.printStackTrace();

        }



        List<HashMap<String, String>> NearbyPlacesList = new ArrayList<>();

        HashMap<String, String> NearbyplaceMap  = null;

        for(int i = 0  ; i < counter ; i++)
        {
            try
            {
                NearbyplaceMap = getSingleNearbyPlace((JSONObject) jsonArray.get(i));
                NearbyPlacesList.add(NearbyplaceMap);

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return NearbyPlacesList;

    }


    public List<HashMap<String, String>> parse(String jSONdata)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject ;


        try
        {
            jsonObject = new JSONObject(jSONdata);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return  getAllNearbyPlace(jsonArray);
    }


}
