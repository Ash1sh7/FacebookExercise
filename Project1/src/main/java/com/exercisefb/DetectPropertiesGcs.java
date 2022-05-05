package com.exercisefb;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ColorInfo;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DetectPropertiesGcs {
	/*
  public static void detectPropertiesGcs() throws IOException {
	  // TODO(developer): Replace these variables before running the sample.
	  String filePath = "https://storage.cloud.google.com/project14628/1.jpg?authuser=0";
	  detectPropertiesGcs(filePath);
  }
  */
  // Detects image properties such as color frequency from the specified remote image on Google
  // Cloud Storage.
 public static void detectProperties(String filePath,LinkedHashMap<String, Float> map) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).setMaxResults(256).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          return;
        }
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (checkIfImageExists1(datastore, FbPhotoId) == false) {  
          //TODO: change fun name 
          Object url;
      DominantColorsAnnotation colors = getImageLabels(url)
          Map<String, Float> map1 = new HashMap<>();

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        DominantColorsAnnotation colors1 = res.getImagePropertiesAnnotation().getDominantColors();
        float red=0,green=0,blue=0;
        for (ColorInfo color : colors.getColorsList()) {
    
       
          red += color.getColor().getRed() * color.getPixelFraction();
          green += color.getColor().getGreen() * color.getPixelFraction();
          blue += color.getColor().getBlue() * color.getPixelFraction();
          //map.put(color.getColor().getRed()+""+color.getColor().getGreen()+""+color.getColor().getBlue(),color.getPixelFraction());
        }
        map.put("red", red);
        map.put("green", green);
        map.put("blue", blue);
      }
    }
  }
}
  public static void addImageDetailsToDataStore(String url, Map<String,Float> colors, String imageId, DatastoreService
          datastore) {  
      Entity User_Images = new Entity(Uploaded_Photos);
      (Uploaded_Photos).setProperty("image_id", imageId);
      (Uploaded_Photos).setProperty("image_url", url);
      StringBuffer result = new StringBuffer();
      String red = "Red: "+colors.get("red");
      String green = "Green: "+colors.get("green");
      String blue = "blue: "+colors.get("blue");
      result.append(red+" "+green+" "+blue);
      (Uploaded_photos).setProperty("labels", result.toString()); //converting labels result to a string
      datastore.put(Uploaded_Photos);
  }

  private void getImageFromStore(HttpServletRequest request, HttpServletResponse response, DatastoreService datastore, String imageId, String dominantColor) {

      Query query =
              new Query("Uploaded_Photos") //checking if images exist in datastore
                      .setFilter(new Query.FilterPredicate("image_id", Query.FilterOperator.EQUAL, imageId));
      PreparedQuery q = datastore.prepare(query);
      List<Entity> results = q.asList(FetchOptions.Builder.withDefaults());
      if(null != results) { //if image exists get url, labels and set them to imageUrl, imageLabels
          results.forEach(user_Photo -> {
              String labelsFromStore = (String) user_Photo.getProperty("labels");
              String image_url=((System) user_Photo).getProperty("image_url").toString();
              request.setAttribute("imageUrl",image_url );
              request.setAttribute("imageLabels", labelsFromStore);
              request.setAttribute("dominantColor", dominantColor);
              RequestDispatcher dispatcher = getServletContext() //sending client request to jsp file
                      .getRequestDispatcher("/upload.jsp");
              try {
                  dispatcher.forward(request, response);
              } catch (ServletException e) {
                  e.printStackTrace();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          });
      }
  private DominantColorsAnnotation getImageLabels(String imageUrl) {
        try {
            byte[] imgBytes = downloadFile(new URL(imageUrl));
            ByteString byteString = ByteString.copyFrom(imgBytes);
            Image image = Image.newBuilder().setContent(byteString).build();
            
            
            
            Feature feature = Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build(); //label detection feature for the image is done here
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request); //labels that has been detected is added to the new list we hv created
            
            ImageAnnotatorClient client = ImageAnnotatorClient.create(); //The ImageAnnotator service returns detected entities from the images
            BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
            client.close();
            List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
            AnnotateImageResponse imageResponse = imageResponses.get(0);
            if (imageResponse.hasError()) {
                System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
                return null;
            }
            return imageResponse.getImagePropertiesAnnotation().getDominantColors();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
  }

  public static boolean checkIfImageExists(DatastoreService datastore, String imageId) {
      Query q =
              new Query("User_Images") //checking if image exists in datastore
                      .setFilter(new Query.FilterPredicate("image_id", Query.FilterOperator.EQUAL, imageId));
      PreparedQuery pq = datastore.prepare(q);
      com.google.appengine.api.datastore.Entity result = pq.asSingleEntity();
      if (result == null) {
          return false;//image doen't exists
      }
      return true;//image exists
  }
