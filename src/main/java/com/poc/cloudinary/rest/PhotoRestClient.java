package com.poc.cloudinary.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.Base64Coder;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import com.poc.cloudinary.util.CloudinaryProperties;

public class PhotoRestClient {

	private static final String CLOUD_NAME = "CLOUD_NAME";
	private static final String API_KEY = "API_KEY";
	private static final String API_SECRET = "API_SECRET";
	
	private static final Map<String, String> api_credentials = CloudinaryProperties.CREDENTIALS.getApiCredentials();

	@SuppressWarnings({"rawtypes","unchecked"})
	public Response getImageList_Cloudinary(String nextCursor, String size) {
		Map config = ObjectUtils.asMap("cloud_name", api_credentials.get(CLOUD_NAME), "api_key",
				api_credentials.get(API_KEY), "api_secret", api_credentials.get(API_SECRET));
		Cloudinary cloudinary = new Cloudinary(config);
		try {
			Map<String, String> paramMap = new HashMap<>();
			if(!org.apache.commons.lang3.StringUtils.isBlank(nextCursor)) {
				paramMap = ObjectUtils.asMap("next_cursor",nextCursor);
			} else {
				paramMap = ObjectUtils.emptyMap();
			}
			Map<Object,Object> result = cloudinary.api().resources(paramMap);
			if(result != null && !result.isEmpty()) {
				return Response.ok(result).build();
			} 
			return Response.status(400).build();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Response getImageList_JerseyClient(String nextCursor, String size) {
		Client client = ClientBuilder.newClient();
		WebTarget imageListWebTarget = client
				.target(buildGetUriWithParams(nextCursor, size));
		Invocation.Builder invocationBuilder = imageListWebTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder.header("Authorization","Basic " + Base64Coder.encodeString(api_credentials.get(API_KEY) + ":" + api_credentials.get(API_SECRET)));

		Response response = invocationBuilder.get();
		return response;
	}
	
	private String buildGetUriWithParams(String nextCursor, String size) {
		StringBuilder uri = new StringBuilder();
		boolean addAmpersand = false;
		uri.append("https://api.cloudinary.com/v1_1/" + api_credentials.get(CLOUD_NAME)
						+ "/resources/image");
		if(!org.apache.commons.lang3.StringUtils.isBlank(nextCursor)) {
			uri = addAmpersand == true ? uri.append("&") : uri.append("?");
			uri.append("next_cursor=").append(nextCursor);
			addAmpersand = true;
		}
		if(!org.apache.commons.lang3.StringUtils.isBlank(size)) {
			uri = addAmpersand == true ? uri.append("&") : uri.append("?");
			uri.append("max_results=").append(size);
			addAmpersand = true;
		}
		return uri.toString();
	}

	@SuppressWarnings("resource")
	public Response uploadImage_jersey(String filePath, String preferredName, List<Transformation> transformations) {
		
		FormDataBodyPart formDataBodyPart = null;
		FormDataMultiPart multiPart = null;
		Response response = null;
		Client client = null;
		String fileName = null;
		try {
			long timestamp = System.currentTimeMillis() / 1000L; 
			client = ClientBuilder.newBuilder()
					.register(MultiPartFeature.class).build();
			WebTarget webTarget = client.target("http://api.cloudinary.com/v1_1/"
					+ api_credentials.get(CLOUD_NAME) + "/auto/upload");
			
			File file = new File(filePath);
			fileName = file.getName();
			Path path = Paths.get(filePath);
			String public_id = null;
			if(preferredName == null) {
				public_id = fileName.substring(0, fileName.lastIndexOf("."));
			} else {
				public_id = preferredName;
			}
			
			StringBuilder image_transform = new StringBuilder();
			if(transformations != null && transformations.size() > 0) {
				
				boolean addPipe = false;
				for(Transformation t : transformations) {
					if(addPipe) {
						image_transform.append("|");
					}
					if(!StringUtils.isBlank(t.getHeight())) {
						image_transform.append("h_").append(t.getHeight()).append(",");
					}
					if(!StringUtils.isBlank(t.getWidth())) {
						image_transform.append("w_").append(t.getWidth()).append(",");
					}
					if(!StringUtils.isBlank(t.getCrop())) {
						image_transform.append("c_").append(t.getCrop()).append(",");
					}
					if(!StringUtils.isBlank(t.getGravity())) {
						image_transform.append("g_").append(t.getGravity());
					}

					addPipe = true;
				}	
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("eager", image_transform.toString());
			params.put("moderation", "manual");
			params.put("public_id", public_id);
			params.put("tags", "rain forest,colorful");
			params.put("timestamp", timestamp + api_credentials.get(API_SECRET));
			params.put("use_filename", "true");
			//params.put("unique_filename", "false");
			Invocation.Builder invocationBuilder = webTarget
					.request(MediaType.APPLICATION_JSON); 
			
			invocationBuilder.header("public_id", public_id);
			invocationBuilder.header("timestamp", timestamp);
			String signature = apiSignRequest(params, api_credentials.get(API_SECRET));
			invocationBuilder.header("signature", signature);
			invocationBuilder.header("api_key", api_credentials.get(API_KEY));

			multiPart = new FormDataMultiPart();
			multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

			byte[] data;
			try {
				data = Files.readAllBytes(path);
			} catch (IOException e) {
				throw new RuntimeException("Error occurred while accessing image to upload " + file.getName(), e);
			}
			formDataBodyPart = new FormDataBodyPart(FormDataContentDisposition
					.name("file").fileName(fileName).build(),
					new ByteArrayInputStream(data),
					MediaType.APPLICATION_OCTET_STREAM_TYPE);
			multiPart.bodyPart(formDataBodyPart);
			multiPart.field("eager", image_transform.toString(), MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("moderation", "manual", MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("public_id", public_id,
					MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("tags", "rain forest,colorful",
					MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("timestamp", String.valueOf(timestamp) + api_credentials.get(API_SECRET),
					MediaType.MULTIPART_FORM_DATA_TYPE);
			
			// Eager transformations
			/*List<Map<String, Object>> transformations = new LinkedList<>();
			Map<String, Object> transformationMap1 = new HashMap<>();
			transformationMap1.put("width", "50");
			transformationMap1.put("height", "40");
			transformationMap1.put("crop", "scale");
			
			Map<String, Object> transformationMap2 = new HashMap<>();
			transformationMap2.put("width", "30");
			transformationMap2.put("height", "30");
			transformationMap2.put("crop", "crop");
			transformationMap2.put("gravity", "south");
			
			transformations.add(transformationMap1);
			transformations.add(transformationMap2);*/

			
			//multiPart.field("unique_filename", String.valueOf(false),
				//	MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("use_filename", String.valueOf(true),
					MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("signature", signature,
					MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.field("api_key", api_credentials.get(API_KEY), MediaType.MULTIPART_FORM_DATA_TYPE);

			response = invocationBuilder.post(Entity.entity(multiPart,
					multiPart.getMediaType()));
			int responseCode = response.getStatus();

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed with HTTP error code : "
						+ responseCode);
			}
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Error occurred while accessing image to upload " + fileName, ex);
		}
		finally{
			formDataBodyPart.cleanup();
			multiPart.cleanup();
        }		
	}

	@SuppressWarnings("rawtypes")
	private String apiSignRequest(Map<String, Object> paramsToSign,
			String apiSecret) {
		Collection<String> params = new ArrayList<String>();
		for (Map.Entry<String, Object> param : new TreeMap<String, Object>(
				paramsToSign).entrySet()) {
			if (param.getValue() instanceof Collection) {
				params.add(param.getKey() + "="
						+ StringUtils.join((Collection) param.getValue(), ","));
			} else {
				if (StringUtils.isNotBlank(param.getValue())) {
					params.add(param.getKey() + "="
							+ param.getValue().toString());
				}
			}
		}
		String to_sign = StringUtils.join(params, "&");
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
		byte[] digest = md.digest(getUTF8Bytes(to_sign + apiSecret));
		return StringUtils.encodeHexString(digest);
	}

	private byte[] getUTF8Bytes(String string) {
		try {
			return string.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	@SuppressWarnings("unchecked")
	public Response uploadPhoto_cloudinary(String filePath, String preferredName) {
		String public_id = null;
		Map config = ObjectUtils.asMap("cloud_name", api_credentials.get(CLOUD_NAME), "api_key",
				api_credentials.get(API_KEY), "api_secret", api_credentials.get(API_SECRET));
		Cloudinary cloudinary = new Cloudinary(config);
		try {
			File file = new File(filePath);
			String fileName = file.getName();
			if(preferredName == null) {
				public_id = fileName.substring(0, fileName.lastIndexOf("."));
			} else {
				public_id = preferredName;
			}
			Map<Object, Object> uploadResult = cloudinary
					.uploader()
					.upload(new File(filePath),
							ObjectUtils.asMap("resource_type", "auto",
									"public_id", public_id, "use_filename",
									true, "unique_filename", false));
			
			if(uploadResult != null && !uploadResult.isEmpty()) {
				return Response.ok(uploadResult).build();
			} 
			return Response.status(400).build();

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Response uploadVideo_cloudinary(String filePath, String preferredName) {
		String public_id = null;
		Map config = ObjectUtils.asMap("cloud_name", api_credentials.get(CLOUD_NAME), "api_key",
				api_credentials.get(API_KEY), "api_secret", api_credentials.get(API_SECRET));
		Cloudinary cloudinary = new Cloudinary(config);
		try {
			File file = new File(filePath);
			String fileName = file.getName();
			if(preferredName == null) {
				public_id = fileName.substring(0, fileName.lastIndexOf("."));
			} else {
				public_id = preferredName;
			}
			Map<Object, Object> uploadResult = cloudinary
					.uploader()
					.upload(new File(filePath),
							ObjectUtils.asMap("resource_type", "video",
									"public_id", public_id, "use_filename",
									true, "unique_filename", false));
			
			if(uploadResult != null && !uploadResult.isEmpty()) {
				return Response.ok(uploadResult).build();
			} 
			return Response.status(400).build();

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	public static void main(String[] args) {
		PhotoRestClient photoClient = new PhotoRestClient();
		//String filePath = "/Users/Code/Downloads/Fanatics/cloudinary/untitled_2.txt";
		//String filePath = "/Users/Code/Downloads/Dec 2014 Associate.pdf";
		//String filePath = "/Users/Code/Downloads/Fanatics/cloudinary/video/Sample Videos (52) - Copy.mp4-SD.mp4";
		String filePath = "/Users/Code/Downloads/Fanatics/cloudinary/images/index.jpg";
		// Jersey Client
		// System.out.println(getImageList_JerseyClient());
		// Cloudinary client
		
		 /*//** Map<Object, Object> resultMap = getImageList(); for
		 * (Map.Entry<Object, Object> entry : resultMap.entrySet()) {
		 * System.out.println(entry.getKey());
		 * System.out.println(entry.getValue().toString()); }*/
		 
		//Response response = photoClient.getImageList_JerseyClient(null, null);
		//Map<Object, Object> imageMap = response.readEntity(Map.class);
				//photoClient.getImageList_Cloudinary();
		//Response response = photoClient.uploadVideo_cloudinary(filePath, "horse1");
		Response response = photoClient.uploadImage_jersey(filePath, "rain999", buildTransformations());
		System.out.println(response.getStatus());
		//Response response = photoClient.postMultipart(filePath, "donkey123");
		//System.out.println(response.readEntity(String.class));
	}
	
	private static List<Transformation> buildTransformations() {
		List<Transformation> transformations = new ArrayList<>();
		Transformation t1 = new Transformation("50", "40", "scale", null);
		Transformation t2 = new Transformation("30", "30", "crop", "south");

		transformations.add(t1);
		transformations.add(t2);
		return transformations;
	}

}
