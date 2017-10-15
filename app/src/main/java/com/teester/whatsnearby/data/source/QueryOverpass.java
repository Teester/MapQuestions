package com.teester.whatsnearby.data.source;

import android.content.Context;
import android.location.Location;

import com.teester.whatsnearby.UseCase;
import com.teester.whatsnearby.data.OsmObject;
import com.teester.whatsnearby.data.OsmObjectType;
import com.teester.whatsnearby.data.PoiList;
import com.teester.whatsnearby.data.PoiTypes;
import com.teester.whatsnearby.data.location.Notifier;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QueryOverpass implements SourceContract.Overpass {

	private static final String TAG = QueryOverpass.class.getSimpleName();
	private Location queryLocation;
	private List<OsmObject> poiList = new ArrayList<OsmObject>();
	private Context context;

	public QueryOverpass(Context context) {
		this.context = context;
	}

	public QueryOverpass() {
	}

	/**
	 * Gets an overpass query url from a given location and accuracy
	 *
	 * @param latitude  location latitude
	 * @param longitude location longitude
	 * @param accuracy  accuracy of location in metres
	 * @return an Overpass url getting items in a radius of the location
	 */
	@Override
	public String getOverpassUri(double latitude, double longitude, float accuracy) {
		if (accuracy < 20) {
			accuracy = 20;
		}

		// Build the Overpass query
		// getting the centre of nodes, ways and relations a given radius around a location for different types
		String overpassLocation = String.format("around:%s,%s,%s", accuracy, latitude, longitude);
		String nwr = "node['%2$s'](%1$s);way['%2$s'](%1$s);relation['%2$s'](%1$s);";

		String shop = String.format(nwr, overpassLocation, "shop");
		String amenity = String.format(nwr, overpassLocation, "amenity");
		String leisure = String.format(nwr, overpassLocation, "leisure");
		String tourism = String.format(nwr, overpassLocation, "tourism");

		String overpassUrl = String.format("http://www.overpass-api.de/api/interpreter?data=[out:json][timeout:25];(%s%s%s%s);out%%20center%%20meta%%20qt;", shop, amenity, leisure, tourism);

		return overpassUrl;
	}

	@Override
	public String queryOverpassApi(String urlString) {

		String resultToDisplay = "";
		InputStream in = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return e.getMessage();
		}

		try {
			resultToDisplay = IOUtils.toString(in, "UTF-8");
			//to [convert][1] byte stream to a string
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultToDisplay;
	}

	/**
	 * Arrange the locations by distance from query location
	 */
	private void sortPoiList() {
		// Arrange the locations by distance from location
		Collections.sort(poiList, new Comparator<OsmObject>() {
			public int compare(OsmObject p1, OsmObject p2) {
				Location location1 = new Location("dummyprovider");
				location1.setLongitude(p1.getLongitude());
				location1.setLatitude(p1.getLatitude());
				Location location2 = new Location("dummyprovider");
				location2.setLongitude(p2.getLongitude());
				location2.setLatitude(p2.getLatitude());
				return (int) (queryLocation.distanceTo(location1) - queryLocation.distanceTo(location2));
			}
		});
	}

	@Override
	public void processResult(String result) {
		if (result != null) {
			try {
				JSONObject jsonObj = new JSONObject(result);

				// Getting JSON Array node
				JSONArray elements = jsonObj.getJSONArray("elements");

				// looping through All elements
				for (int i = 0; i < elements.length(); i++) {
					JSONObject e = elements.getJSONObject(i);
					if (e.has("tags")) {
						JSONObject tags = e.getJSONObject("tags");
						if (tags.has("name")) {

							int id = e.getInt("id");

							String osmType = e.getString("type");

							if (e.has("center")) {
								e = e.getJSONObject("center");
							}
							double lat = e.getDouble("lat");
							double lon = e.getDouble("lon");
							Location elementLocation = new Location("dummyprovider");
							elementLocation.setLatitude(lat);
							elementLocation.setLongitude(lon);
							float distance = queryLocation.distanceTo(elementLocation);

							String name = tags.getString("name");

							String type = getType(tags);

							OsmObjectType poitype = PoiTypes.getPoiType(type);
							if (poitype != null) {
								OsmObject object = new OsmObject(id, osmType, name, type, lat, lon, distance);
								poiList.add(object);
							}
						}
					}
				}
			} catch (final JSONException e) {
			}

			sortPoiList();
			prepareNotification();
		}
	}

	@Override
	public void queryOverpass(Location location) {

		this.queryLocation = location;
		String overpassUrl = getOverpassUri(location.getLatitude(), location.getLongitude(), location.getAccuracy());
		String overpassQuery = queryOverpassApi(overpassUrl);
		processResult(overpassQuery);
	}

	/**
	 * Gets the type of location e.g. hairdresser, pharmacy, stadium etc
	 *
	 * @param tags - the JSON object of the location
	 * @return - the location type
	 * @throws JSONException - if it's not an amenity, shop, tourism or leisure
	 */
	String getType(JSONObject tags) throws JSONException {
		String type = "";
		if (tags.has("amenity")) {
			type = tags.getString("amenity");
		}
		if (tags.has("shop")) {
			type = tags.getString("shop");
		}
		if (tags.has("tourism")) {
			type = tags.getString("tourism");
		}
		if (tags.has("leisure")) {
			type = tags.getString("leisure");
		}
		return type;
	}

	/**
	 * Prepares the notification to be displayed.
	 */
	private void prepareNotification() {
		if (poiList.size() > 0) {
			PoiList.getInstance().setPoiList(poiList);

			OsmObject poi = poiList.get(0);
			OsmObjectType type = PoiTypes.getPoiType(poi.getType());
			int drawable = type.getObjectIcon();
			Notifier.createNotification(context, poi.getName(), drawable);
		} else {
			Notifier.cancelNotifictions(context);
		}
	}

	public static final class response implements UseCase.RequestValues {
		String response = "";
	}
}
