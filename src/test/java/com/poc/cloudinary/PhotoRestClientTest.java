package com.poc.cloudinary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;

import com.poc.cloudinary.rest.PhotoRestClient;

public class PhotoRestClientTest {
	private PhotoRestClient photoRestClient = new PhotoRestClient();
	
	@Test
	public void testGetImageList_Cloudinary() {
		Response response = photoRestClient.getImageList_Cloudinary(null, null);
		assertEquals(response.getStatus(), HttpStatus.SC_OK);
		
		Map<Object, Object> imageMap = (Map<Object, Object>)response.getEntity();
		List<Map<String, String>> imageResourceList = (List<Map<String, String>>) imageMap.get("resources");
		
		assertNotNull(response);
		
		Map<String, String> image_1 = imageResourceList.get(0);
		assertThat(image_1.get("public_id"), is(not(nullValue())));
		assertThat(image_1.get("public_id"), not(isEmptyString()));
		assertThat(image_1.get("url"), is(not(nullValue())));
		assertThat(image_1.get("url"), not(isEmptyString()));
		assertThat(image_1.get("format"), is(not(nullValue())));
		assertThat(image_1.get("format"), not(isEmptyString()));
		assertThat(image_1.get("created_at"), is(not(nullValue())));
		assertThat(image_1.get("created_at"), not(isEmptyString()));
		assertThat(image_1.get("secure_url"), is(not(nullValue())));
		assertThat(image_1.get("secure_url"), not(isEmptyString()));

	}
	
	@Test
	public void testGetImageListWithNextCursor_Cloudinary() {
		Response response = photoRestClient.getImageList_Cloudinary("f145b61b1c9d552fce1abe59fbbdbf06", "10");
		assertEquals(response.getStatus(), HttpStatus.SC_OK);
		
		Map<Object, Object> imageMap = (Map<Object, Object>)response.getEntity();
		List<Map<String, String>> imageResourceList = (List<Map<String, String>>) imageMap.get("resources");
		
		assertNotNull(response);
		
		Map<String, String> image_1 = imageResourceList.get(0);
		assertThat(image_1.get("public_id"), is(not(nullValue())));
		assertThat(image_1.get("public_id"), not(isEmptyString()));
		assertThat(image_1.get("url"), is(not(nullValue())));
		assertThat(image_1.get("url"), not(isEmptyString()));
		assertThat(image_1.get("format"), is(not(nullValue())));
		assertThat(image_1.get("format"), not(isEmptyString()));
		assertThat(image_1.get("created_at"), is(not(nullValue())));
		assertThat(image_1.get("created_at"), not(isEmptyString()));
		assertThat(image_1.get("secure_url"), is(not(nullValue())));
		assertThat(image_1.get("secure_url"), not(isEmptyString()));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getImageList_JerseyClient() {
		Response response = photoRestClient.getImageList_JerseyClient(null, null);
		Map<Object, Object> imageMap = response.readEntity(Map.class);
		
		assertNotNull(response);
		assertEquals(response.getStatus(), HttpStatus.SC_OK);

		List<Map<String, String>> imageResourceList = (List<Map<String, String>>) imageMap.get("resources");
		Map<String, String> image_1 = imageResourceList.get(0);
		assertThat(image_1.get("public_id"), is(not(nullValue())));
		assertThat(image_1.get("public_id"), not(isEmptyString()));
		assertThat(image_1.get("url"), is(not(nullValue())));
		assertThat(image_1.get("url"), not(isEmptyString()));
		assertThat(image_1.get("format"), is(not(nullValue())));
		assertThat(image_1.get("format"), not(isEmptyString()));
		assertThat(image_1.get("created_at"), is(not(nullValue())));
		assertThat(image_1.get("created_at"), not(isEmptyString()));
		assertThat(image_1.get("secure_url"), is(not(nullValue())));
		assertThat(image_1.get("secure_url"), not(isEmptyString()));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getImageListWithNextCursor_JerseyClient() {
		Response response = photoRestClient.getImageList_JerseyClient("f145b61b1c9d552fce1abe59fbbdbf06", "10");
		Map<Object, Object> imageMap = response.readEntity(Map.class);
		
		assertNotNull(response);
		assertEquals(response.getStatus(), HttpStatus.SC_OK);

		List<Map<String, String>> imageResourceList = (List<Map<String, String>>) imageMap.get("resources");
		Map<String, String> image_1 = imageResourceList.get(0);
		assertThat(image_1.get("public_id"), is(not(nullValue())));
		assertThat(image_1.get("public_id"), not(isEmptyString()));
		assertThat(image_1.get("url"), is(not(nullValue())));
		assertThat(image_1.get("url"), not(isEmptyString()));
		assertThat(image_1.get("format"), is(not(nullValue())));
		assertThat(image_1.get("format"), not(isEmptyString()));
		assertThat(image_1.get("created_at"), is(not(nullValue())));
		assertThat(image_1.get("created_at"), not(isEmptyString()));
		assertThat(image_1.get("secure_url"), is(not(nullValue())));
		assertThat(image_1.get("secure_url"), not(isEmptyString()));

	}
	
	@Test
	public void test_uploadImage_JerseyClient() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("index.jpg").getFile());
		Response response = photoRestClient.uploadImage_jersey(file.getAbsolutePath(), "donkey101", null);
		Map<String, String> imageResource = (Map<String, String>) response.readEntity(Map.class);
		
		assertNotNull(response);
		assertEquals(response.getStatus(), HttpStatus.SC_OK);

		assertThat(imageResource.get("public_id"), is(not(nullValue())));
		assertThat(imageResource.get("public_id"), not(isEmptyString()));
		assertThat(imageResource.get("url"), is(not(nullValue())));
		assertThat(imageResource.get("url"), not(isEmptyString()));

	}
	
	@Test
	public void test_uploadImage_CloudinaryClient() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("index.jpg").getFile());
		Response response = photoRestClient.uploadPhoto_cloudinary(file.getAbsolutePath(), "donkey102");
		Map<String, String> imageResource = (Map<String, String>) response.getEntity();
		
		assertNotNull(response);
		assertEquals(response.getStatus(), HttpStatus.SC_OK);

		assertThat(imageResource.get("public_id"), is(not(nullValue())));
		assertThat(imageResource.get("public_id"), not(isEmptyString()));
		assertThat(imageResource.get("url"), is(not(nullValue())));
		assertThat(imageResource.get("url"), not(isEmptyString()));

	}
}
