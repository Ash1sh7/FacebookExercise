package com.exercisefb;



import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/hello"}
)
class HelloAppEngine extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		OutputStream out = null;
		InputStream filecontent = null;
		//final String path = "C:/Users/ash2n/Desktop/Project1/src/main/webapp/";
		final String path = System.getProperty("user.dir")+"/";
		System.out.println(path);
		
		//final String path = "/";
		Part filePart = request.getPart("fileToUpload");		
		final String fileName = getFileName(filePart);
		try {
			out = new FileOutputStream(new File(new File("/WEB-INF/")+ "files" + File.separator
	                + fileName));
			filecontent = filePart.getInputStream();
			//path +"ImageMapping/"
	        int read = 0;
	        final byte[] bytes = new byte[1024];
	
	        while ((read = filecontent.read(bytes)) != -1) {
	            out.write(bytes, 0, read);
	        } 
	        LinkedHashMap<String,Float> userMap = new LinkedHashMap<>();
	     
	        System.out.println("Calculating user image properties");
	        DetectProperties.detectProperties(new File("/WEB-INF/")+ "files" + File.separator + fileName,userMap);
	        
	        float maxVal = userMap.get("red");
	        String mergeFilePath = "ImageMapping/red.jpg";
	        if(userMap.get("green") > maxVal) {
	        	mergeFilePath = "ImageMapping/green.jpg";
	        }
	        if(userMap.get("blue") > maxVal) {
	        	mergeFilePath = "ImageMapping/blue.jpg";
	        }

	        int scaledWidth = 1024;
	        int scaledHeight = 768;
	        resize(new File("/WEB-INF/")+ "files" + File.separator + fileName, new File("/WEB-INF/")+ "files"+ File.separator + fileName, scaledWidth, scaledHeight);
	        resize(path + mergeFilePath, path + mergeFilePath, scaledWidth, scaledHeight);

	        int type;
	        int chunkWidth, chunkHeight;
	        BufferedImage[] buffImages = new BufferedImage[2];
	        buffImages[0] = ImageIO.read(new File("/WEB-INF/"+ "files"+ File.separator + fileName));
	        buffImages[1] = ImageIO.read(new File(path + mergeFilePath));
	        type = buffImages[0].getType();
	        chunkWidth = buffImages[0].getWidth();
	        chunkHeight = buffImages[0].getHeight();
	        
	        //Initializing the final image
	        BufferedImage finalImg = new BufferedImage(chunkWidth*2, chunkHeight, type);
	        
	        finalImg.createGraphics().drawImage(buffImages[0],0,0, null);
	        finalImg.createGraphics().drawImage(buffImages[1],chunkWidth,0, null);
	        String basePath = null;
			ImageIO.write(finalImg, "jpg", new File(basePath + "finalImg.jpg"));
	        
	        response.setContentType("image/jpg");  
	        ServletOutputStream sout;  //taking final image to servlet output stream
	        sout = response.getOutputStream();  
	        FileInputStream fin = new FileInputStream(basePath + "finalImg.jpg");  
	          
	        BufferedInputStream bin = new BufferedInputStream(fin);  
	        BufferedOutputStream bout = new BufferedOutputStream(sout);  
	        int ch =0; ;  
	        while((ch=bin.read())!=-1)  
	        {  
	        bout.write(ch);  
	        }  
	          
	        bin.close();  
	        fin.close();  
	        bout.close();  
	        sout.close();        
	        
	        //response.getOutputStream().println("<p>Thanks! Here's the image you uploaded:</p>");
			//response.getOutputStream().println("<img src=\""  + "/tmp/finalImg.jpg" +"?r="+Math.random() + "\" />");
			//response.getOutputStream().println("<p>Upload another image <a href=\"http://localhost:8080/index.html\">here</a>.</p>");	

			
		} catch (FileNotFoundException fne) {
			response.getOutputStream().println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
			response.getOutputStream().println("<br/> ERROR: " + fne.getMessage());

	    } finally {
	        if (out != null) {
	            out.close();
	        }
	        if (filecontent != null) {
	            filecontent.close();
	        }
	        
	    }
	}
	
	private String getFileName(final Part part) {
	    for (String content : part.getHeader("content-disposition").split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(
	                    content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}
	
	public static void resize(String inputImagePath,
            String outputImagePath, double percent) throws IOException {
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
    }
	
	public static void resize(String inputImagePath,
            String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
 
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);
 
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}